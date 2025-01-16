package top.crossoverjie.cicada.server.intercept;

import java.util.Comparator;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/10/21 19:45
 * @since JDK 1.8
 */
public class InterceptorOrderComparator implements Comparator<AbstractInterceptor> {


    @Override
    public int compare(AbstractInterceptor o1, AbstractInterceptor o2) {

        if (o1.getOrder() <= o2.getOrder()){
            return 1 ;
        }

        return 0;
    }
}
