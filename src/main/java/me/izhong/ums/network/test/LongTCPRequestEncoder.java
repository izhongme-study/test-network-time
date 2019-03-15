package me.izhong.ums.network.test;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;

import java.text.DecimalFormat;


@Slf4j
public class LongTCPRequestEncoder extends MessageToByteEncoder<LongTCPMsg>
{
    private String charset;

    public LongTCPRequestEncoder(String charset) {
        super();
        this.charset = charset;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, LongTCPMsg message, ByteBuf byteBuf)
            throws Exception {
        try {
            /*  报文体  */
            String payload = message.getPayload();
            byte[] data = payload.getBytes(charset);
            /*  报文长度  */
            int length = data.length;
            // 在发送心跳的时候需要考虑到长度域
            DecimalFormat df = new DecimalFormat("0000");
            String lengthStr = df.format(length);
            byte[] lengthField = lengthStr.getBytes();

            if(ConfigBean.showLog)
                log.info("发送TCP长连接报文 内容 : {} {}", lengthStr, payload);
            /* 填充报文 */
            byteBuf.writeBytes(lengthField);
            byteBuf.writeBytes(data);
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
