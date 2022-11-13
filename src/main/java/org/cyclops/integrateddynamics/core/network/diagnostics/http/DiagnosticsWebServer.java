package org.cyclops.integrateddynamics.core.network.diagnostics.http;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.logging.log4j.Level;
import org.cyclops.integrateddynamics.IntegratedDynamics;

/**
 * An HTTP server that holds a single channel.
 * @author rubensworks
 */
public class DiagnosticsWebServer {

    private final int port;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    public DiagnosticsWebServer(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getUrl() {
        return String.format("http://localhost:%s/", getPort());
    }

    public Channel getChannel() {
        return channel;
    }

    public void initialize() {
        IntegratedDynamics.clog(Level.INFO, "Starting local Integrated Dynamics network diagnostics server...");
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new DiagnosticsWebServerInitializer());

        try {
            this.channel = b.bind(this.port).sync().channel();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        IntegratedDynamics.clog(Level.INFO, "Started local Integrated Dynamics network diagnostics server on http://localhost:" + this.port + "/");
    }

    public void deinitialize() {
        IntegratedDynamics.clog(Level.INFO, "Stopping local Integrated Dynamics network diagnostics server...");
        if (this.bossGroup != null && workerGroup != null) {
            try {
                bossGroup.shutdownGracefully().sync();
                workerGroup.shutdownGracefully().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        IntegratedDynamics.clog(Level.INFO, "Stopped local Integrated Dynamics network diagnostics server");
    }

}
