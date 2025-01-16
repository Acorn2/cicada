package top.crossoverjie.cicada.base.annotation;


import java.lang.annotation.*;

/**
 * 强依赖Web相关的组件
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CicadaAction {

    String value() default "" ;
}
