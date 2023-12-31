package com.tdj.common.utils;

import com.alibaba.nacos.common.utils.StringUtils;
import com.tdj.common.annotation.Utils;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Utils
@Slf4j
public class RedisUtils {
    public void set(Vertx vertx, String key, String value){
        JsonObject body = new JsonObject();
        body.put("key",key);
        body.put("value",value);
        vertx.eventBus().send("redis.set",body);
    }
    public void setEx(Vertx vertx, String key,long interval, String value){
        JsonObject body = new JsonObject();
        body.put("key",key);
        body.put("value",value);
        body.put("interval",interval);
        vertx.eventBus().send("redis.setex",body);
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
    public void del(Vertx vertx, String key){
        vertx.eventBus().send("redis.del",key);
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
    public void psubscribeKeyExpired(Vertx vertx, String key, String value, long interval, Handler<AsyncResult<Message<JsonObject>>> replyHandler) {
        JsonObject body = new JsonObject();
        body.put("key",key);
        body.put("value",value);
        vertx.eventBus().request("redis.psubscribe_key_expired",body, replyHandler);
    }

    /**
     * 获取锁，true 则得到锁，false 已被锁定
     * @param lockName       锁名称
     * @param lockExoire     锁时间
     * @return
     */
    public Future<Boolean> getLock(Vertx vertx, String lockName, long lockExoire, Handler<AsyncResult<Message<JsonObject>>> replyHandler) {
        Promise<Boolean> promise = Promise.promise();
        // 获取过期时间点的毫秒值
        long expireAt = System.currentTimeMillis() + lockExoire + 1;
        Future<Boolean> setNxFuture = setNx(vertx,lockName,String.valueOf(expireAt));
        setNxFuture.onComplete(setNxHandler->{
            if (setNxHandler.succeeded()) {
                Boolean acquire = setNxHandler.result();
                if (acquire) {
                    promise.complete(true);
                } else {
                    Future<String> getFuture = get(vertx,lockName);
                    getFuture.onComplete(getHandler->{
                        if (getHandler.succeeded()) {
                            String expireTimeStr = getHandler.result();
                            if (StringUtils.isNotBlank(expireTimeStr)) {
                                try {
                                    long expireTime = Long.parseLong(expireTimeStr);
                                    // 如果锁已经过期
                                    if (expireTime < System.currentTimeMillis()) {
                                        Future<String> getSetFuture = getSet(vertx,lockName,String.valueOf(System.currentTimeMillis() + lockExoire + 1));
                                        getSetFuture.onComplete(getSetHandler->{
                                            if (getSetHandler.succeeded()) {
                                                promise.complete(Long.parseLong(new String(getSetHandler.result())) < System.currentTimeMillis());
                                            } else {
                                                promise.complete(false);
                                            }
                                        });
                                    }
                                } catch (Exception e) {
                                    log.error("",e);
                                    promise.complete(false);
                                }
                            }
                        } else {
                            promise.complete(false);
                        }
                    }).onFailure(getErr-> {
                        promise.complete(false);
                    });
                }
            } else {
                promise.complete(false);
            }
        }).onFailure(setNxErr->{
            promise.complete(false);
        });
        return promise.future();
    }
}