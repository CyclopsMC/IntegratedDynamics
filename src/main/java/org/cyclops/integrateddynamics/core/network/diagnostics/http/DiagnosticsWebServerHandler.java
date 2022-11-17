package org.cyclops.integrateddynamics.core.network.diagnostics.http;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import org.cyclops.integrateddynamics.core.network.diagnostics.NetworkDataClient;

/**
 * A handler for HTTP requests.
 * @author rubensworks
 */
public class DiagnosticsWebServerHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, Object message) throws Exception {
        if (message instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) message;

            if (HttpUtil.is100ContinueExpected(request)) {
                FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);
                context.write(response);
            }

            HttpResponseStatus responseStatus;
            String responseString;
            String contentType;
            switch (request.uri()) {
                case "/":
                    responseStatus = HttpResponseStatus.OK;
                    responseString = "<html>Hello World</html>"; // TODO
                    contentType = "text/html; charset=UTF-8";
                    break;
                case "/data.json":
                    responseStatus = HttpResponseStatus.OK;
                    responseString = NetworkDataClient.getAsJsonString();
                    contentType = "application/json; charset=UTF-8";
                    break;
                case "/teleport":
                    responseStatus = HttpResponseStatus.OK;
                    responseString = "Ok"; // TODO
                    contentType = "text/plain; charset=UTF-8";
                    break;
                default:
                    responseStatus = HttpResponseStatus.NOT_FOUND;
                    responseString = "Not found";
                    contentType = "text/plain; charset=UTF-8";
                    break;
            }

            if (!writeResponse(request, context, responseString, contentType, responseStatus)) {
                // If keep-alive is off, close the connection once the content is fully written.
                context.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }
    }

    private boolean writeResponse(HttpRequest request, ChannelHandlerContext context,
                                  String responseString, String contentType,
                                  HttpResponseStatus responseStatus) {
        // Decide whether to close the connection or not.
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, responseStatus,
                Unpooled.copiedBuffer(responseString, CharsetUtil.UTF_8));

        response.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
            // Add keep alive header as per:
            // - http://www.w3.org/Protocols/HTTP/1.1/draft-ietf-http-v11-spec-01.html#Connection
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
        }

        // Write the response.
        context.write(response);

        return keepAlive;
    }
}
