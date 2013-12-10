/*
 * #%L
 * ACS AEM Commons Bundle
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.acs.livereload.LiveReloadServer;

@Component(immediate = true, metatype = true)
@Service
public class LiveReloadServerImpl implements LiveReloadServer {

    private static final Logger logger = LoggerFactory.getLogger(LiveReloadServerImpl.class);

    private static final int DEFAULT_PORT = 35729;

    @Property(intValue = DEFAULT_PORT, label = "Port", description = "Web Socket Port")
    private static final String PROP_PORT = "port";

    private static final String[] DEFAULT_PREFIXES = { "/cf", "/content", "/etc" };

    @Property(value = { "/cf", "/content", "/etc" }, label = "Path Prefixes", description = "Path prefixes")
    private static final String PROP_PREFIXES = "prefixes";

    private int port;

    private boolean running;

    private Channel channel;

    private NioEventLoopGroup broadcastGroup;

    private DefaultChannelGroup group;

    private Map<Channel, ChannelInfo> infos;

    private NioEventLoopGroup bossGroup;

    private NioEventLoopGroup workerGroup;

    private ContentPageMatcher matcher;

    private String[] pathPrefixes;

    @Activate
    protected void activate(ComponentContext ctx) throws Exception {
        this.port = PropertiesUtil.toInteger(ctx.getProperties().get(PROP_PORT), DEFAULT_PORT);
        this.pathPrefixes = PropertiesUtil.toStringArray(ctx.getProperties().get(PROP_PREFIXES), DEFAULT_PREFIXES);
        this.broadcastGroup = new NioEventLoopGroup(1);

        this.group = new DefaultChannelGroup("live-reload", broadcastGroup.next());
        this.infos = new ConcurrentHashMap<Channel, ChannelInfo>();

        this.matcher = new ContentPageMatcher();

        startServer();
        running = true;
    }

    @Deactivate
    protected void deactivate() throws InterruptedException {
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
                ;
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().sync();
            }
        }
    }

    private void stopServer() throws InterruptedException {
        channel.close().sync();
    }

    private void startServer() throws Exception {
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .childHandler(new WebSocketServerInitializer());

        this.channel = b.bind(this.port).sync().channel();

        logger.info("Web socket server started at port {}.", port);
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
            pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
            pipeline.addLast("handler", new WebSocketServerHandler(group, infos));
        }
    }

    class ContentPageMatcher implements ChannelMatcher {
        public boolean matches(Channel channel) {
            ChannelInfo info = infos.get(channel);
            if (info != null && info.supported && info.uri != null) {
                String path = info.uri.getPath();
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
