package top.crossoverjie.cicada.server.handle;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import top.crossoverjie.cicada.base.log.LoggerBuilder;
import top.crossoverjie.cicada.server.action.param.Param;
import top.crossoverjie.cicada.server.action.param.ParamMap;
import top.crossoverjie.cicada.server.action.req.CicadaHttpRequest;
import top.crossoverjie.cicada.server.action.req.CicadaRequest;
import top.crossoverjie.cicada.server.action.res.ApiResponse;
import top.crossoverjie.cicada.server.action.res.CicadaHttpResponse;
import top.crossoverjie.cicada.server.action.res.CicadaResponse;
import top.crossoverjie.cicada.server.bean.CicadaBeanManager;
import top.crossoverjie.cicada.server.config.AppConfig;
import top.crossoverjie.cicada.server.constant.CicadaConstant;
import top.crossoverjie.cicada.server.context.CicadaContext;
import top.crossoverjie.cicada.base.exception.CicadaException;
import top.crossoverjie.cicada.server.exception.handle.GlobalHandelException;
import top.crossoverjie.cicada.server.intercept.InterceptorChainManager;
import top.crossoverjie.cicada.server.route.MethodInvokeProcessor;
import top.crossoverjie.cicada.server.scanner.RouteAnnotationScanner;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Function:
 * SimpleChannelInboundHandler的优势
 * 自动处理消息的释放
 * 类型安全（泛型支持）
 * 简化消息处理逻辑
 * 自动进行类型转换
 *
 * @author crossoverJie
 * Date: 2018/8/30 18:47
 * @since JDK 1.8
 */

/*
 * 标记Handler可以被多个Channel共享
 * 允许在多个ChannelPipeline中重用同一个Handler实例
 * 通过@ChannelHandler.Sharable注解实现
 */
@ChannelHandler.Sharable
public final class HttpDispatcher extends SimpleChannelInboundHandler<HttpRequest> {

    private static final Logger LOGGER = LoggerBuilder.getLogger(HttpDispatcher.class);

    /**
     * 单例模式保证
     * 避免重复创建实例
     * 确保线程安全
     */
    private final AppConfig appConfig = AppConfig.getInstance();
    private final InterceptorChainManager interceptProcess = InterceptorChainManager.getInstance();
    private final RouteAnnotationScanner routerScanner = RouteAnnotationScanner.getInstance();
    private final MethodInvokeProcessor methodInvokeProcessor = MethodInvokeProcessor.getInstance();
    private final CicadaBeanManager cicadaBeanManager = CicadaBeanManager.getInstance();
    private final GlobalHandelException exceptionHandle = cicadaBeanManager.exceptionHandle();
    private Exception exception;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpRequest httpRequest) {

        LOGGER.info("Received request: {} {}", httpRequest.method(), httpRequest.uri());

        CicadaRequest cicadaRequest = null;
        if (httpRequest instanceof DefaultHttpRequest) {
            DefaultHttpRequest defaultHttpRequest = (DefaultHttpRequest) httpRequest;
            LOGGER.info("HttpDispatcher channelRead0 error, request is not FullHttpRequest");
            cicadaRequest = CicadaHttpRequest.init(defaultHttpRequest, null);
        } else if (httpRequest instanceof FullHttpRequest) {
            LOGGER.info("HttpDispatcher channelRead0 error, request is FullHttpRequest");
            FullHttpRequest fullRequest = (FullHttpRequest) httpRequest;
            // 从 FullHttpRequest 中获取请求体内容
            String content = fullRequest.content().toString(CharsetUtil.UTF_8);

            cicadaRequest = CicadaHttpRequest.init(fullRequest, content);
        }

        CicadaResponse cicadaResponse = CicadaHttpResponse.init();

        // set current thread request and response
        CicadaContext.setContext(new CicadaContext(cicadaRequest, cicadaResponse));

        try {
            // request uri
            String uri = cicadaRequest.getUrl();
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(URLDecoder.decode(httpRequest.uri(), "utf-8"));

            // check Root Path
            appConfig.checkRootPath(uri, queryStringDecoder);

            // route Action
            //Class<?> actionClazz = routeAction(queryStringDecoder, appConfig);

            //build paramMap
            Param paramMap = buildParamMap(queryStringDecoder);

            //load interceptors
            interceptProcess.loadInterceptors();

            //interceptor before
            boolean access = interceptProcess.processBefore(paramMap);
            if (!access) {
                // 权限验证失败
                // 限流控制
                return;
            }

            // execute Method
            /**
             * GET /user/get?userId=123
             *
             * 1. 路由匹配 -> /user/get
             * 2. 找到对应Method -> getUser
             * 3. 获取UserAction实例
             * 4. 构建参数 -> Param对象
             * 5. 反射调用方法
             * 6. 处理返回值
             */
            Method method = routerScanner.routeMethod(queryStringDecoder);
            Object result = methodInvokeProcessor.invoke(method);

            LOGGER.info("Method invoke result : {}", JSON.toJSONString(result));
            handleMethodResult(result);

            //WorkAction action = (WorkAction) actionClazz.newInstance();
            //action.execute(CicadaContext.getContext(), paramMap);


            // interceptor after
            interceptProcess.processAfter(paramMap);

        } catch (Exception e) {
            exceptionCaught(ctx, e);
        } finally {
            // Response
            responseContent(ctx);

            // remove cicada thread context
            CicadaContext.removeContext();
        }
    }

    /**
     * 处理方法调用的返回结果
     *
     * @param result 方法返回值
     */
    private void handleMethodResult(Object result) {
        if (result == null) {
            return;
        }

        if (result instanceof ApiResponse) {
            // 如果返回值已经是ApiResponse类型，直接转JSON
            CicadaContext.getContext().json((ApiResponse) result);
        } else if (result instanceof String) {
            // 字符串类型，默认作为text处理
            CicadaContext.getContext().text((String) result);
        } else {
            // 其他类型返回值，包装成ApiResponse后转JSON
            ApiResponse ApiResponse = new ApiResponse();
            ApiResponse.setData(result);
            CicadaContext.getContext().json(ApiResponse);
        }
    }

    /**
     * Response
     *
     * @param ctx ChannelHandlerContext
     */
    private void responseContent(ChannelHandlerContext ctx) {
        // 1. 从上下文获取响应内容
        CicadaResponse cicadaResponse = CicadaContext.getResponse();
        String context = cicadaResponse.getHttpContent();

        // 2. 将字符串内容转换为ByteBuf
        ByteBuf buf = Unpooled.wrappedBuffer(context.getBytes(StandardCharsets.UTF_8));

        // 3. 创建HTTP响应对象
        DefaultFullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1,    // HTTP版本
                HttpResponseStatus.OK,    // 状态码
                buf                      // 响应内容
        );

        // 4. 构建响应头
        buildHeader(response);

        // 5. 写入响应并刷新
        ctx.writeAndFlush(response);
    }

    /**
     * build paramMap
     *
     * @param queryStringDecoder
     * @return
     */
    private Param buildParamMap(QueryStringDecoder queryStringDecoder) {
        Map<String, List<String>> parameters = queryStringDecoder.parameters();
        Param paramMap = new ParamMap();
        for (Map.Entry<String, List<String>> stringListEntry : parameters.entrySet()) {
            String key = stringListEntry.getKey();
            List<String> value = stringListEntry.getValue();
            paramMap.put(key, value.get(0));
        }
        return paramMap;
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (CicadaException.isResetByPeer(cause.getMessage())) {
            return;
        }
        
        try {
            cicadaBeanManager.exceptionHandle().resolveException(
                CicadaContext.getContext(), 
                (Exception) cause
            );
        } catch (Exception e) {
            LOGGER.error("Error handling exception", e);
        }
    }

    /**
     * build Header
     *
     * @param response
     */
    private void buildHeader(DefaultFullHttpResponse response) {
        CicadaResponse cicadaResponse = CicadaContext.getResponse();

        HttpHeaders headers = response.headers();
        headers.setInt(HttpHeaderNames.CONTENT_LENGTH, response.content().readableBytes());
        headers.set(HttpHeaderNames.CONTENT_TYPE, cicadaResponse.getContentType());

        List<Cookie> cookies = cicadaResponse.cookies();
        for (Cookie cookie : cookies) {
            headers.add(CicadaConstant.ContentType.SET_COOKIE, io.netty.handler.codec.http.cookie.ServerCookieEncoder.LAX.encode(cookie));
        }

    }
}
