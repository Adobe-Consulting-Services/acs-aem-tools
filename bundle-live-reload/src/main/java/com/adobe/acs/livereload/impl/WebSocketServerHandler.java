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
import static io.netty.handler.codec.http.HttpHeaders.*;
import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * See http://feedback.livereload.com/knowledgebase/articles/86174-livereload-
 * protocol
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {

    private ChannelGroup group;

    private Map<Channel, ChannelInfo> infos;

    public WebSocketServerHandler(ChannelGroup group, Map<Channel, ChannelInfo> infos) {
        this.group = group;
        this.infos = infos;
    }

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);

    private static final String WEBSOCKET_PATH = "/websocket";

    private WebSocketServerHandshaker handshaker;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        if ("/".equals(req.getUri()) || "/favicon.ico".equals(req.getUri())) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }

        if (req.getUri().startsWith("/livereload.js")) {
            InputStream is = getClass().getResourceAsStream("/livereload.js");
            byte[] data = IOUtils.toByteArray(is);
            ByteBuf content = Unpooled.wrappedBuffer(data);

            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

            res.headers().set(CONTENT_TYPE, "application/javascript");
            setContentLength(res, content.readableBytes());

            sendHttpResponse(ctx, req, res);
            return;
        }

        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req),
                null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

        // Check for closing frame
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
                    .getName()));
        }

        String request = ((TextWebSocketFrame) frame).text();
        try {
            JSONObject obj = new JSONObject(request);
            handleCommand(obj, ctx);

        } catch (JSONException e) {
            throw new IllegalArgumentException(String.format("%s is not a valid JSON object"));
        }

    }

    private void handleCommand(JSONObject obj, ChannelHandlerContext ctx) throws JSONException {
        String cmd = obj.getString(COMMAND);
        Channel channel = ctx.channel();
        if (CMD_HELLO.equals(cmd)) {
            JSONObject result = new JSONObject();
            result.put(COMMAND, CMD_HELLO);
            JSONArray protocols = new JSONArray();
            protocols.put(PROTOCOL_VERSION_7);
            result.put(PROTOCOLS, protocols);
            result.put("serverName", "AEM Live Reload Server");

            channel.write(new TextWebSocketFrame(result.toString()));
            
            if (isSupported(obj)) {
                logger.info("adding LiveReload channel");
                group.add(channel);
            }

            ChannelInfo info = new ChannelInfo();
            info.supported = isSupported(obj);
            infos.put(channel, info);
        } else if (CMD_INFO.equals(cmd)) {
            ChannelInfo info = infos.get(channel);
            if (info != null) {
                String url = obj.getString(URL);
                try {
                    URI uri = new URI(url);
                    info.uri = uri;
                    
                    logger.info("added uri to channel info {}", info);
                    
                } catch (URISyntaxException e) {
                    logger.warn("Unable to store uri " + url, e);
                }
            }
        } else {
            logger.warn("Unknown command {}", cmd);
            logger.info(obj.toString(2));
        }
    }

    private boolean isSupported(JSONObject obj) {
        try {
            if (obj.has(PROTOCOLS)) {
                JSONArray protocols = obj.getJSONArray(PROTOCOLS);
                for (int i = 0; i < protocols.length(); i++) {
                    String protocol = protocols.getString(i);
                    if (PROTOCOL_VERSION_7.equals(protocol)) {
                        return true;
                    }
                }
            }
        } catch (JSONException e) {
        }
        return false;
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("caught exception", cause);
        ctx.close();
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        return "ws://" + req.headers().get(HOST) + WEBSOCKET_PATH;
    }
}