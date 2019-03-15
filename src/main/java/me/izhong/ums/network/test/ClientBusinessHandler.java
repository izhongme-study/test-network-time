package me.izhong.ums.network.test;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import me.izhong.ums.network.test.collect.StatisticsService;

import java.io.IOException;


@Slf4j
public class ClientBusinessHandler extends ChannelInboundHandlerAdapter {

    private StatisticsService statisticsService;

    public ClientBusinessHandler(StatisticsService statisticsService){
        this.statisticsService = statisticsService;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 握手
        log.info("客户端请求成功：{}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端连接关闭：{}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, final Object obj)
            throws Exception {
        if (obj instanceof LongTCPMsg) {
            LongTCPMsg bs = (LongTCPMsg) obj;
            String msg = bs.getPayload();
            if(ConfigBean.showLog)
                log.info("receive:{}", msg);
            long v = Long.valueOf(msg);
            long now = System.currentTimeMillis();
            statisticsService.accumulate("TEST","TEST_TCP", "SUCC", 1, now - v);
        } else {
            throw new Exception("消息类型错误");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if (cause instanceof IOException) {
            log.error("通讯异常：{}, {}", cause.getMessage(), ctx.channel()
                    .remoteAddress());
        } else if (cause instanceof DecoderException) {
            Throwable c = cause.getCause();
            if (c != null)
                cause = c;
            log.error("解码异常：{}, {}", cause.getMessage(), ctx.channel()
                    .remoteAddress());
        } else {
            log.error("未捕获异常：{}", ctx.channel().remoteAddress(), cause);
        }

        ctx.channel().close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        if (evt instanceof IdleStateEvent) {
            log.info("连接超时，强制关闭：{}", ctx.channel().remoteAddress());
            ctx.channel().close();
        }
    }


}
