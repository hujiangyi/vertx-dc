package com.tdj.common.utils;

import com.alibaba.nacos.common.utils.StringUtils;
import com.tdj.common.annotation.Utils;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Utils
@Slf4j
public class RedisUtils {
    public Future<Void> set(Vertx vertx, String key, String value){
        Promise<Void> promise = Promise.promise();
        JsonObject body = new JsonObject();
        body.put("key",key);
        body.put("value",value);
        vertx.eventBus().request("redis.set",body, (AsyncResult<Message<Void>> handler) ->{
            if (handler.succeeded()){
                promise.complete(null);
            } else {
                promise.fail(handler.cause());
            }
        });
        return promise.future();
    }
    public Future<Void> setEx(Vertx vertx, String key,long interval, String value){
        Promise<Void> promise = Promise.promise();
        JsonObject body = new JsonObject();
        body.put("key",key);
        body.put("value",value);
        body.put("interval",interval);
        vertx.eventBus().request("redis.setex",body, (AsyncResult<Message<Void>> handler) ->{
            if (handler.succeeded()){
                promise.complete(null);
            } else {
                promise.fail(handler.cause());
            }
        });
        return promise.future();
    }
    public Future<Boolean> setNx(Vertx vertx, String key, String value){
        Promise<Boolean> promise = Promise.promise();
        JsonObject body = new JsonObject();
        body.put("key",key);
        body.put("value",value);
        vertx.eventBus().request("redis.setnx",body, (AsyncResult<Message<Boolean>> handler) ->{
            if (handler.succeeded()){
                promise.complete(handler.result().body());
            } else {
                promise.fail(handler.cause());
            }
        });
        return promise.future();
    }
    public Future<String> get(Vertx vertx, String key){
        Promise<String> promise = Promise.promise();
        vertx.eventBus().request("redis.get",key, (AsyncResult<Message<String>> handler) ->{
            if(handler.succeeded()) {
                promise.complete(handler.result().body());
            } else {
                promise.fail(handler.cause());
            }
        });
        return promise.future();
    }
    public Future<String> getSet(Vertx vertx, String key, String value){
        Promise<String> promise = Promise.promise();
        JsonObject body = new JsonObject();
        body.put("key",key);
        body.put("value",value);
        vertx.eventBus().request("redis.getset",body, (AsyncResult<Message<String>> handler) ->{
            if(handler.succeeded()) {
                promise.complete(handler.result().body());
            } else {
                promise.fail(handler.cause());
            }
        });
        return promise.future();
    }
    public Future<Void> del(Vertx vertx, String key){
        Promise<Void> promise = Promise.promise();
        vertx.eventBus().request("redis.del",key, (AsyncResult<Message<Void>> handler) ->{
            if (handler.succeeded()){
                promise.complete(null);
            } else {
                promise.fail(handler.cause());
            }
        });
        return promise.future();
    }
    public Future<JsonArray> keys(Vertx vertx, String prefix){
        Promise<JsonArray> promise = Promise.promise();
        vertx.eventBus().request("redis.keys",prefix, (AsyncResult<Message<JsonArray>> handler) ->{
            if(handler.succeeded()) {
                promise.complete(handler.result().body());
            } else {
                promise.fail(handler.cause());
            }
        });
        return promise.future();
    }
    public Future<String> psubscribeKeyExpired(Vertx vertx, String key, String value, long interval) {
        Promise<String> promise = Promise.promise();
        JsonObject body = new JsonObject();
        body.put("key",key);
        body.put("value",value);
        body.put("interval",interval);
        vertx.eventBus().request("redis.psubscribe_key_expired",body, (AsyncResult<Message<String>> handler) ->{
            if(handler.succeeded()) {
                promise.complete(handler.result().body());
            } else {
                promise.fail(handler.cause());
            }
        });
        return promise.future();
    }

    /**
     * 获取锁，true 则得到锁，false 已被锁定
     * @param lockName       锁名称
     * @param lockExpire     锁时间
     * @return
     */
    public Future<Boolean> getLock(Vertx vertx, String lockName, long lockExpire) {
        Promise<Boolean> promise = Promise.promise();
        // 获取过期时间点的毫秒值
        long expireAt = System.currentTimeMillis() + lockExpire + 1;
        Future<Boolean> setNxFuture = setNx(vertx,lockName,String.valueOf(expireAt));
        setNxFuture.compose(acquire->{
            if (acquire) {
                promise.complete(true);
                return Future.failedFuture("");
            } else {
                return get(vertx,lockName);
            }
        }).compose(expireTimeStr->{
            if (StringUtils.isNotBlank(expireTimeStr)) {
                long expireTime = Long.parseLong(expireTimeStr);
                // 如果锁已经过期
                if (expireTime < System.currentTimeMillis()) {
                    return getSet(vertx,lockName,String.valueOf(System.currentTimeMillis() + lockExpire + 1));
                }
            }
            promise.complete(false);
            return Future.failedFuture("");
        }).compose(handler->{
            promise.complete(Long.parseLong(handler) < System.currentTimeMillis());
            return Future.succeededFuture(true);
        });
        return promise.future();
    }

    public Future<Void> delLock(Vertx vertx, String key){
        return del(vertx,key);
    }
}
