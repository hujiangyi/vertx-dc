package com.tdj.common;

import com.tdj.common.annotation.Component;
import com.tdj.common.annotation.Utils;
import com.tdj.common.annotation.mysql.Dao;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Properties;

@Slf4j
public abstract class BaseComponent implements ModuleInit {
    public Future<Boolean> init(Vertx vertx, Properties nacosConfig, JsonObject config){
        String className = this.getClass().getName();
        log.info("{} init.",className);
        Promise<Boolean> promise = Promise.promise();
        vertx.eventBus().consumer("init doInjection", message -> {
            doInjection();
        });
        return promise.future();
    }

    private void doInjection() {
        Class clazz = this.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Class<?> type = field.getType();
            Annotation[] annotations = type.getDeclaredAnnotations();
            for (Annotation annotation:annotations) {
                if (annotation instanceof Component || annotation instanceof Dao || annotation instanceof Utils) {
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
}
