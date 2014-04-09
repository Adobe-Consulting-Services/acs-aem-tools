/*
 * #%L
 * ACS AEM Tools Bundle
 * %%
 * Copyright (C) 2013 Adobe
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.adobe.acs.livereload.impl;

import static com.adobe.acs.livereload.impl.LiveReloadConstants.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.group.ChannelMatcher;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Filter;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.acs.livereload.LiveReloadServer;

@Component(immediate = true, metatype = true, label = "ACS AEM Tools - Live Reload Server",
        description = "AEM Live Reload web socket server")
@Service
public final class LiveReloadServerImpl implements LiveReloadServer {

    private static final Logger log = LoggerFactory.getLogger(LiveReloadServerImpl.class);

    private static final boolean DEFAULT_FILTER_ENABLED = false;

    private static final int DEFAULT_PORT = 35729;
    
    private static final int MAX_CONTENT_LENGTH = 65536;

    @Property(label = "JS Injection Enabled?",
            description = "Enable the injection of the JavaScript library into all HTML pages.",
            boolValue = DEFAULT_FILTER_ENABLED)
    private static final String PROP_FILTER_ENABLED = "prop.filter.enabled";

    @Property(intValue = DEFAULT_PORT, label = "Port", description = "Web Socket Port")
    private static final String PROP_PORT = "port";

    private static final String[] DEFAULT_PREFIXES = { "/cf", "/content", "/etc", "/editor.html" };

    @Property(value = { "/cf", "/content", "/etc", "/editor.html" }, label = "Path Prefixes", description = "Path prefixes")
    private static final String PROP_PREFIXES = "prefixes";

    private int port;
    private static final int FILTER_ORDER = -3000;

    private boolean running;

    private Channel serverChannel;

    private NioEventLoopGroup broadcastGroup;

    private DefaultChannelGroup group;

    private Map<Channel, ChannelInfo> infos;

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    private ContentPageMatcher matcher;
    private ServiceRegistration filterReference;

    private String[] pathPrefixes;

    @Activate
    protected void activate(ComponentContext ctx) throws Exception {
        Dictionary<?, ?> props = ctx.getProperties();
        this.port = PropertiesUtil.toInteger(props.get(PROP_PORT), DEFAULT_PORT);
        this.pathPrefixes = PropertiesUtil.toStringArray(props.get(PROP_PREFIXES), DEFAULT_PREFIXES);
        this.broadcastGroup = new NioEventLoopGroup(1);

        this.group = new DefaultChannelGroup("live-reload", broadcastGroup.next());
        this.infos = new ConcurrentHashMap<Channel, ChannelInfo>();

        this.matcher = new ContentPageMatcher();

        startServer();
        running = true;

        if (PropertiesUtil.toBoolean(props.get(PROP_FILTER_ENABLED), DEFAULT_FILTER_ENABLED)) {
            Dictionary<Object, Object> filterProps = new Hashtable<Object, Object>();
            filterProps.put("sling.filter.scope", "request");
            filterProps.put("filter.order", FILTER_ORDER);
            filterReference = ctx.getBundleContext().registerService(Filter.class.getName(),
                    new JavaScriptInjectionFilter(port, pathPrefixes), filterProps);
        }
    }

    @Deactivate
    protected void deactivate() throws InterruptedException {
        if (filterReference != null) {
            filterReference.unregister();
            filterReference = null;
        }

        try {
            if (running) {
                try {
                    stopServer();
                } finally {
                    running = false;
                }
            }
        } finally {
            if (broadcastGroup != null) {
                broadcastGroup.shutdownGracefully().sync();
            }
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().sync();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().sync();
            }
        }
    }

    private void stopServer() throws InterruptedException {
        serverChannel.close().sync();
    }

    private void startServer() throws Exception {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new WebSocketServerInitializer());

        this.serverChannel = b.bind(this.port).sync().channel();

        log.info("Web socket server started at port {}.", port);
    }

    public void triggerReload(String path) throws JSONException {
        if (group != null) {
            JSONObject reload = createReloadObject(path);
            group.flushAndWrite(new TextWebSocketFrame(reload.toString()), matcher);
        }
    }

    private JSONObject createReloadObject(String includePath) throws JSONException {
        JSONObject reload = new JSONObject();
        reload.put(COMMAND, CMD_RELOAD);
        reload.put(PATH, includePath);
        reload.put("liveCSS", true);
        return reload;
    }

    class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            pipeline.addLast("codec-http", new HttpServerCodec());
            pipeline.addLast("aggregator", new HttpObjectAggregator(MAX_CONTENT_LENGTH));
            pipeline.addLast("handler", new WebSocketServerHandler(group, infos));
        }
    }

    class ContentPageMatcher implements ChannelMatcher {
        public boolean matches(Channel channel) {
            ChannelInfo info = infos.get(channel);
            if (info != null && info.isSupported() && info.getUri() != null) {
                String path = info.getUri().getPath();
                for (String prefix : pathPrefixes) {
                    if (path.startsWith(prefix)) {
                        return true;
                    }
                }
                return false;
            }

            return false;
        }
    }
}
