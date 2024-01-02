package com.tdj.common;

import com.tdj.common.annotation.mysql.Dao;
import com.tdj.common.annotation.Utils;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Properties;

@Slf4j
public abstract class BaseVerticle extends AbstractVerticle {
    protected Properties nacosConfig = new Properties();
    protected EventBus eventBus;
    public void start() {
        eventBus = vertx.eventBus();
        eventBus.consumer("nacos.config_init_success", message -> {
            log.info("nacos config init success----------->");
            try {
                nacosConfig = new Properties();
                nacosConfig.load(new StringReader(message.body().toString()));
                doInit();
            } catch (IOException e) {
                log.error("",e);
            }
        });
        eventBus.consumer("nacos.config_renew", message -> {
            log.info("nacos config renew----------->" + message.toString());
            try {
                nacosConfig = new Properties();
                nacosConfig.load(new StringReader(message.body().toString()));
                doRenewConfig();
            } catch (IOException e) {
                log.error("",e);
            }
        });
        eventBus.consumer("init doInjection", message -> {
            doInjection();
        });
    }

    private void doInjection() {
        Class clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Class<?> type = field.getType();
            Annotation[] annotations = type.getDeclaredAnnotations();
            for (Annotation annotation:annotations) {
                if (annotation instanceof Dao || annotation instanceof Utils) {
                    field.setAccessible(true);
                    try {
                        field.set(this, Contact.beanMap.get(annotation.annotationType().getName()).get(type));
                    } catch (IllegalAccessException e) {
                        log.error("",e);
                    }
                }
            }
        }
    }

    protected void doInit() {}

    protected void doRenewConfig() {}
}
