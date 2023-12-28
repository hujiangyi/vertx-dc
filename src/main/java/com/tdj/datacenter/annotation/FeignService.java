package com.tdj.datacenter.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface FeignService {
    String value() default "";
}
