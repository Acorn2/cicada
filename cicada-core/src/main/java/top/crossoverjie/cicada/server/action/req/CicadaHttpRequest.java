package top.crossoverjie.cicada.server.action.req;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import top.crossoverjie.cicada.server.action.cookie.HttpCookie;
import top.crossoverjie.cicada.server.action.param.Param;
import top.crossoverjie.cicada.server.action.param.ParamMap;
import top.crossoverjie.cicada.server.constant.CicadaConstant;
import top.crossoverjie.cicada.server.exception.GenericException;

import java.util.HashMap;
import java.util.Map;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/10/5 00:42
 * @since JDK 1.8
 */
public class CicadaHttpRequest implements CicadaRequest {

    private String method ;

    private String url ;

    private String clientAddress ;

    private Param parameters;

    private Param bodyParameters;  // 新增：处理请求体参数

    private Map<String, HttpCookie> cookie = new HashMap<>(8) ;
    private Map<String,String> headers = new HashMap<>(8) ;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private CicadaHttpRequest(){}

    public static CicadaHttpRequest init(HttpRequest httpRequest, String content) {
        CicadaHttpRequest request = new CicadaHttpRequest() ;
        request.method = httpRequest.method().name();
        request.url = httpRequest.uri();

        //build headers
        buildHeaders(httpRequest, request);

        //initBean cookies
        initCookies(request);

        // 1. 处理 URL 查询参数 (GET)
        ParamMap queryParams = new ParamMap();
        QueryStringDecoder decoder = new QueryStringDecoder(httpRequest.uri());
        decoder.parameters().forEach((key, values) -> {
            queryParams.put(key, values.get(0));
        });
        request.parameters = queryParams;

        // 2. 处理请求体参数 (POST)
        if (HttpMethod.POST.name().equals(httpRequest.method().name())) {
            ParamMap bodyParams = new ParamMap();
            String contentType = httpRequest.headers().get("Content-Type");
            
            if (contentType != null && content != null && !content.isEmpty()) {
                if (contentType.contains("application/json")) {
                    try {
                        Map<String, Object> jsonMap = OBJECT_MAPPER.readValue(content, Map.class);
                        bodyParams.putAll(jsonMap);
                    } catch (Exception e) {
                        throw new GenericException("Failed to parse JSON body: " + e.getMessage());
                    }
                }
                // ... 其他 content-type 处理 ...
            }
            request.bodyParameters = bodyParams;
        }

        return request ;
    }

    /**
     * build headers
     * @param httpRequest io.netty.httprequest
     * @param request cicada request
     */
    private static void buildHeaders(HttpRequest httpRequest, CicadaHttpRequest request) {
        for (Map.Entry<String, String> entry : httpRequest.headers().entries()) {
            request.headers.put(entry.getKey(),entry.getValue());
        }
    }

    /**
     * initBean cookies
     * @param request request
     */
    private static void initCookies(CicadaHttpRequest request) {
        for (Map.Entry<String, String> entry : request.headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (!key.equals(CicadaConstant.ContentType.COOKIE)){
                continue;
            }

            for (io.netty.handler.codec.http.cookie.Cookie cookie : ServerCookieDecoder.LAX.decode(value)) {
                HttpCookie cicadaCookie = new HttpCookie() ;
                cicadaCookie.setName(cookie.name());
                cicadaCookie.setValue(cookie.value());
                cicadaCookie.setDomain(cookie.domain());
                cicadaCookie.setMaxAge(cookie.maxAge());
                cicadaCookie.setPath(cookie.path()) ;
                request.cookie.put(cicadaCookie.getName(),cicadaCookie) ;
            }
        }
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public String getUrl() {
        return this.url;
    }

    @Override
    public HttpCookie getCookie(String key) {
        return cookie.get(key) ;
    }

    @Override
    public Param getQueryParameters() {
        return parameters;
    }

    @Override
    public Param getBodyParameters() {
        return bodyParameters;
    }

    @Override
    public Param getAllParameters() {
        // 合并查询参数和请求体参数
        ParamMap allParams = new ParamMap();
        if (parameters != null) {
            allParams.putAll(parameters);
        }
        if (bodyParameters != null) {
            allParams.putAll(bodyParameters);
        }
        return allParams;
    }
}
