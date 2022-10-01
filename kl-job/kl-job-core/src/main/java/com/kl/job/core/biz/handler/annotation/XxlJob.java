package com.kl.job.core.biz.handler.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface XxlJob {
    String value();
    String init() default "";
    String destroy() default "";
}
