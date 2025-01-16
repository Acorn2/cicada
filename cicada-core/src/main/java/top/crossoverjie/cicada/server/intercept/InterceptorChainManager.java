package top.crossoverjie.cicada.server.intercept;

import top.crossoverjie.cicada.server.action.param.Param;
import top.crossoverjie.cicada.server.config.AppConfig;
import top.crossoverjie.cicada.server.context.CicadaContext;
import top.crossoverjie.cicada.server.scanner.ClasLoadersScanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/10/21 18:42
 * @since JDK 1.8
 */
public class InterceptorChainManager {

    private InterceptorChainManager(){}

    private volatile static InterceptorChainManager process ;

    private static List<AbstractInterceptor> interceptors ;

    private AppConfig appConfig = AppConfig.getInstance();

    /**
     * get single Instance
     * @return
     */
    public static InterceptorChainManager getInstance(){
        if (process == null){
            synchronized (InterceptorChainManager.class){
                if (process == null){
                    process = new InterceptorChainManager() ;
                }
            }
        }
        return process ;
    }


    public void loadInterceptors() throws Exception {

        if (interceptors != null){
            return;
        }else {
            interceptors = new ArrayList<>(10) ;
            Map<Class<?>, Integer> cicadaInterceptor = ClasLoadersScanner.getCicadaInterceptor(appConfig.getRootPackageName());
            for (Map.Entry<Class<?>, Integer> classEntry : cicadaInterceptor.entrySet()) {
                Class<?> interceptorClass = classEntry.getKey();
                AbstractInterceptor interceptor = (AbstractInterceptor) interceptorClass.newInstance();
                interceptor.setOrder(classEntry.getValue());
                interceptors.add(interceptor);
            }
            Collections.sort(interceptors,new InterceptorOrderComparator());
        }
    }


    /**
     * execute before
     * @param param
     * @throws Exception
     */
    public boolean processBefore(Param param) throws Exception {
        for (AbstractInterceptor interceptor : interceptors) {
            boolean access = interceptor.before(CicadaContext.getContext(), param);
            if (!access){
                return access ;
            }
        }
        return true;
    }

    /**
     * execute after
     * @param param
     * @throws Exception
     */
    public void processAfter(Param param) throws Exception{
        for (AbstractInterceptor interceptor : interceptors) {
            interceptor.after(CicadaContext.getContext(),param) ;
        }
    }
}
