package top.crossoverjie.cicada.server.init;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import top.crossoverjie.cicada.server.handle.HttpDispatcher;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 17/05/2018 18:51
 * @since JDK 1.8
 */
public class CicadaInitializer extends ChannelInitializer<Channel> {
    // 使用final修饰，只创建一次HttpDispatcher实例
    private final HttpDispatcher httpDispatcher = new HttpDispatcher() ;

    @Override
    public void initChannel(Channel ch) throws Exception {
        // 下面的注释，是处理DefaultHttpRequest请求的
        //  ch.pipeline()
        //          .addLast(new HttpRequestDecoder())
        //          .addLast(new HttpResponseEncoder())
        //          .addLast(new ChunkedWriteHandler())
        //          .addLast(httpDispatcher)
        //          .addLast("logging", new LoggingHandler(LogLevel.INFO));

        /**
         * HttpServerCodec 替代了分开的编码器和解码器，它能够同时处理请求和响应的编解码
         * HttpObjectAggregator 会将HTTP消息的多个部分聚合成完整的FullHttpRequest或FullHttpResponse
         * 设置了最大内容长度为64KB (65536字节)
         *
         * 这样修改后，当收到POST请求时：
         *
         * HttpServerCodec 会首先解码HTTP请求
         *
         * HttpObjectAggregator 会将请求头和请求体聚合成一个 FullHttpRequest
         *
         * 最终传递给 HttpDispatcher 的将是一个完整的 FullHttpRequest 对象
         */
       ch.pipeline()
           .addLast("codec", new HttpServerCodec())
           .addLast("aggregator", new HttpObjectAggregator(65536))
           .addLast("http-chunked", new ChunkedWriteHandler())
           .addLast("dispatcher", httpDispatcher)
           .addLast("logging", new LoggingHandler(LogLevel.INFO));
    }
}
