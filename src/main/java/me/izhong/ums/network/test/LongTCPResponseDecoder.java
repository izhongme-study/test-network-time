package me.izhong.ums.network.test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
public class LongTCPResponseDecoder extends ByteToMessageDecoder
{

    private String charset;

    public LongTCPResponseDecoder(String charset) {
        super();
        this.charset = charset;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list)
            throws Exception {
        if (byteBuf.readableBytes() < 4) {
            if (log.isDebugEnabled()) {
                log.debug("T0前置返回的报文长度有误");
            }
            return;
        }
        byteBuf.markReaderIndex();
        byte[] lengthField = new byte[4];
        byteBuf.readBytes(lengthField);
        String lengthStr = new String(lengthField);
        int length = Integer.parseInt(lengthStr);
        if (length > 10 * 1024) {
            throw new Exception("报文过长");
        }
        if (byteBuf.readableBytes() < length) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] data = new byte[length];
        byteBuf.readBytes(data);

        String payload = new String(data, charset);
        if(ConfigBean.showLog)
            log.info("收到TCP长连接报文 内容 : {} {}", lengthStr, payload);
        list.add(new LongTCPMsg(payload));
    }
}
