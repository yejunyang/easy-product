package ai.yunxi.common.annotation;

import java.lang.annotation.*;

/**
 * 系统级别Controller层自定义注解，拦截Controller
 */
@Target({ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SystemControllerLog {
    String description() default "";
}
