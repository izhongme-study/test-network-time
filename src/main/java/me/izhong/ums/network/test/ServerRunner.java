package me.izhong.ums.network.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class ServerRunner implements ApplicationRunner {

    private static EventLoopGroup group = new NioEventLoopGroup(1);

    @Setter
    @Getter
    private String name="服务端";

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String mode = System.getProperty("mode");
        if(!StringUtils.equals(mode,"server"))
            return;

        int port = new Integer(System.getProperty("port")).intValue();

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.channel(NioServerSocketChannel.class)
                .group(group,group)
        .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelHandler[] handlers = createHandlers();
                for (ChannelHandler handler : handlers) {
                    ch.pipeline().addLast(handler);
                };
            }
        }).option(ChannelOption.SO_BACKLOG, 128)
                .option(ChannelOption.SO_REUSEADDR, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.SO_REUSEADDR, true);
        ChannelFuture cf = serverBootstrap.bind(port).await();
        if (!cf.isSuccess()) {
            log.error("无法绑定端口：" + port);
            throw new Exception("无法绑定端口：" + port);
        }

        log.info("服务[{}]启动完毕，监听端口[{}]", getName(), port);
    }

    private ChannelHandler[] createHandlers() {
        return new ChannelHandler[]{
                new IdleStateHandler(0, 0, 120),
                new LongTCPRequestEncoder("utf-8"),
                new LongTCPResponseDecoder("utf-8"),
                new ServerBusinessHandler()
        };
    }
}
