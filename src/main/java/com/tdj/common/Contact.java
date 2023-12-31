package com.tdj.common;

import io.vertx.core.Vertx;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Contact {
    private static Vertx vertx;
    public static ConcurrentMap<String, Map<Class, Object>> beanMap = new ConcurrentHashMap();
    public static Vertx getVertxInstance(){
        return vertx;
    }
    public static void setVertxInstance(Vertx v){
        vertx = v;
    }
}
