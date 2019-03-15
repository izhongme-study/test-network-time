package me.izhong.ums.network.test;

import com.google.common.util.concurrent.RateLimiter;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import me.izhong.ums.network.test.collect.StatisticsService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class ClientRunner implements ApplicationRunner{

    private static EventLoopGroup group = new NioEventLoopGroup(2);

    private Channel channel;

    private static ExecutorService executorService = Executors.newFixedThreadPool(1);

    private static CountDownLatch countDownLatch = new CountDownLatch(100000);

    @Autowired
    private StatisticsService statisticsService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        String mode = System.getProperty("mode");
        if(!StringUtils.equals(mode,"client"))
            return;

        String host = System.getProperty("host");
        boolean showlog = Boolean.valueOf(System.getProperty("showlog"));

        int port = new Integer(System.getProperty("port")).intValue();

        long tps = new Integer(System.getProperty("tps")).intValue();


        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group).channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3 * 1000);
        bootstrap.option(ChannelOption.TCP_NODELAY,true);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ChannelHandler[] handlers = createHandlers();
                for (ChannelHandler handler : handlers) {
                    ch.pipeline().addLast(handler);
                };
            }
        });

        ChannelFuture conn = bootstrap.connect(host,port).sync();
        if(conn.isSuccess()){
            log.info("连接成功");
            channel = conn.channel();
            //bootstrap.channel()
            executorService.submit(new TokenSender(channel,tps,countDownLatch));
        }
        log.info("等待退出");
        countDownLatch.await(3, TimeUnit.MINUTES);
        log.info("退出");

    }

    private ChannelHandler[] createHandlers() {
        return new ChannelHandler[]{
                new IdleStateHandler(0, 0, 120),
                new LongTCPRequestEncoder("utf-8"),

                new LongTCPResponseDecoder("utf-8"),
                new ClientBusinessHandler(statisticsService)
        };
    }

    static class TokenSender implements Runnable {

        RateLimiter rateLimiter;
        AtomicInteger atomicInteger = new AtomicInteger(0);
        long tps = 1;
        private CountDownLatch countDownLatch;
        private Channel channel;
        TokenSender (Channel channel,long tps, CountDownLatch countDownLatch){
            this.channel = channel;
            this.tps = tps;
            rateLimiter = RateLimiter.create(tps);
            this.countDownLatch =countDownLatch;
        }
        @Override
        public void run() {
            while (true) {
                rateLimiter.acquire();
                long now = System.currentTimeMillis();
                LongTCPMsg msg = new LongTCPMsg(Long.valueOf(now).toString());
                //log.info("runner");
                channel.writeAndFlush(msg);
                countDownLatch.countDown();
            }
        }
    }
}
