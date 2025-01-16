package top.crossoverjie.cicada.base.annotation;

import java.lang.annotation.*;

/**
 * 强依赖Web相关的组件
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CicadaInterceptor {
    int order() default 0 ;
}
