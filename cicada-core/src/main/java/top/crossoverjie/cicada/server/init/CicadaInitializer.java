package top.crossoverjie.cicada.server.init;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
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
        ch.pipeline()
                .addLast(new HttpRequestDecoder())
                .addLast(new HttpResponseEncoder())
                .addLast(new ChunkedWriteHandler())
                .addLast(httpDispatcher)
                .addLast("logging", new LoggingHandler(LogLevel.INFO));
    }
}
