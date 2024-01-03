package com.tdj.common.utils;

import com.alibaba.nacos.common.utils.StringUtils;
import com.tdj.common.ModuleInit;
import com.tdj.common.annotation.Utils;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

@Utils
@Slf4j
public class RedisUtils implements ModuleInit {
    private final AtomicBoolean CONNECTING = new AtomicBoolean();
    private final int MAX_RECONNECT_RETRIES = 16;
    private Vertx vertx;
    private Properties nacosConfig;
    private JsonObject config;
    private Redis redis;
    private RedisAPI api;

    public Future<Boolean> init(Vertx vertx, Properties nacosConfig, JsonObject config){
        log.info("redis init.");
        Promise<Boolean> promise = Promise.promise();
        this.vertx = vertx;
        this.nacosConfig = nacosConfig;
        this.config = config;
        Future<RedisConnection> future = createNormalRedisClient();
        future.onComplete(handler->{
            RedisConnection client = future.result();
            client.exceptionHandler(response->{
                log.error("redis 异常断线！" , response.getCause());
                attemptReconnect(0);
            }).endHandler(response->{
                log.error("redis 掉线！");
                attemptReconnect(0);
            });
            api = RedisAPI.api(client);
            log.info("redis init success!");
            promise.complete(true);
        }).onFailure(err->{
            log.error("redis 初始化异常！");
            promise.fail("Redis 初始化异常！");
        });
        return promise.future();
    }

    public Future<Void> set(String key, String value){
        Promise<Void> promise = Promise.promise();
        List<String> args = new ArrayList<>();
        args.add(key);
        args.add(value);
        api.set(args,handler->{
            if (handler.succeeded()) {
                promise.complete(null);
            } else {
                promise.fail("redis set error.");
            }
        });
        return promise.future();
    }
    public Future<Void> setEx(String key,long interval, String value){
        Promise<Void> promise = Promise.promise();
        api.setex(key,""+interval,value,handler->{
            if (handler.succeeded()) {
                promise.complete(null);
            } else {
                promise.fail("setex error.");
            }
        });
        return promise.future();
    }
    public Future<Boolean> setNx(String key, String value){
        Promise<Boolean> promise = Promise.promise();
        List<String> args = new ArrayList<>();
        args.add(key);
        args.add(value);
        api.setnx(key,value,response->{
            if (response.succeeded()) {
                promise.complete(response.result().toString().equalsIgnoreCase("1"));
            } else {
                promise.fail("set error.");
            }
        });
        return promise.future();
    }
    public Future<String> get(String key){
        Promise<String> promise = Promise.promise();
        api.get(key,handler->{
            if (handler.succeeded()) {
                if (handler.result() == null) {
                    promise.complete(null);
                } else {
                    promise.complete(handler.result().toString());
                }
            } else {
                promise.fail("get error.");
            }
        });
        return promise.future();
    }
    public Future<String> getSet(String key, String value){
        Promise<String> promise = Promise.promise();
        api.getset(key,value,handler->{
            if (handler.succeeded()) {
                if (handler.result() == null) {
                    promise.complete(null);
                } else {
                    promise.complete(handler.result().toString());
                }
            } else {
                promise.fail("getset error.");
            }
        });
        return promise.future();
    }
    public Future<Void> del(String key){
        Promise<Void> promise = Promise.promise();
        List<String> args = new ArrayList<>();
        args.add(key);
        api.del(args,handler->{
            if (handler.succeeded()) {
                promise.complete(null);
            } else {
                promise.fail("del error.");
            }
        });
        return promise.future();
    }
    public Future<JsonArray> keys(String prefix){
        Promise<JsonArray> promise = Promise.promise();
        api.keys(prefix, (AsyncResult<Response> handler) ->{
            if (handler.succeeded()) {
                try {
                    Response response = handler.result();
                    JsonArray reply = response.stream()
                            .map(Object::toString)
                            .collect(JsonArray::new,JsonArray::add,JsonArray::addAll);
                    promise.complete(reply);
                }catch (Exception e) {
                    log.error("",e);
                }
            } else {
                promise.fail("kyes error.");
            }
        });
        return promise.future();
    }
    public Future<String> psubscribeKeyExpired(String key, long interval) {
        Promise<String> promise = Promise.promise();
        Future<RedisConnection> future = createNewRedisClient();
        future.onComplete(complet->{
            final long timerId;
            RedisConnection client = future.result();
            final AtomicBoolean isTimeout = new AtomicBoolean(false);
            //timer设置为interval * 1000 + 10 是为了避免边界异常
            timerId = vertx.setTimer(interval + 10,timer->{
                //未监听到超时事件，无需callback业务端，直接结束本次的连接
                log.info("没有在间隔{}ms内收到{}的超时事件,主动关闭此次redis连接",interval,key);
                promise.fail("超过最大等待时长，没有监听到" + key + "的超时事件，主动退出监听");
                synchronized (isTimeout) {
                    isTimeout.set(true);
                    client.close();
                }
            });
            client.handler(resopnse -> {
                synchronized (isTimeout) {
                    if (resopnse.size() == 4
                            && resopnse.get(0).toString().equalsIgnoreCase("pmessage")
                            && resopnse.get(1).toString().equalsIgnoreCase("__keyevent@*__:expired")
                            && resopnse.get(3).toString().equalsIgnoreCase(key) && !isTimeout.get()) {
                        //收到了过期事件，需要callback业务端处理此事件
                        promise.complete(resopnse.get(3).toString());
                        log.info("监听到{}的超时事件，取消边界检查器",key);
                        vertx.cancelTimer(timerId);
                        client.close();
                    }
                }
            });
            RedisAPI api = RedisAPI.api(client);
            List<String> args = new ArrayList<>();
            args.add("__keyevent@*__:expired");
            api.psubscribe(args, handler->{
                if (handler.succeeded()) {
                    log.info("psubscribe set success");
                }
            });
        }).onFailure(err->{
            promise.fail("redis connect faild!");
        });
        return promise.future();
    }

    /**
     * 获取锁，true 则得到锁，false 已被锁定
     * @param lockName       锁名称
     * @param lockExpire     锁时间
     * @return
     */
    public Future<Boolean> getLock(String lockName, long lockExpire) {
        Promise<Boolean> promise = Promise.promise();
        // 获取过期时间点的毫秒值
        long expireAt = System.currentTimeMillis() + lockExpire + 1;
        Future<Boolean> setNxFuture = setNx(lockName,String.valueOf(expireAt));
        setNxFuture.compose(acquire->{
            if (acquire) {
                promise.complete(true);
                return Future.failedFuture("");
            } else {
                return get(lockName);
            }
        }).compose(expireTimeStr->{
            if (StringUtils.isNotBlank(expireTimeStr)) {
                long expireTime = Long.parseLong(expireTimeStr);
                // 如果锁已经过期
                if (expireTime < System.currentTimeMillis()) {
                    return getSet(lockName,String.valueOf(System.currentTimeMillis() + lockExpire + 1));
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

    public Future<Void> delLock(String key){
        return del(key);
    }

    public Future<Boolean> exists(String key){
        Promise<Boolean> promise = Promise.promise();
        List<String> args = new ArrayList<>();
        args.add(key);
        api.exists(args, response->{
            if (response.succeeded()) {
                promise.complete(response.result().toString().equalsIgnoreCase("1"));
            } else {
                promise.fail("exists error.");
            }
        });
        return promise.future();
    }

    private Future<RedisConnection> createNewRedisClient() {
        Promise<RedisConnection> promise = Promise.promise();
        RedisOptions redisOptions = new RedisOptions()
                .addConnectionString(nacosConfig.getProperty("redis.host"))
                .setType(RedisClientType.STANDALONE);
        log.info("redis NetClientOptions is {}" ,redisOptions.getNetClientOptions().toJson());
        redis = Redis.createClient(vertx,redisOptions);
        redis.connect(handler->{
            if (handler.succeeded()) {
                RedisConnection client = handler.result();
                promise.complete(client);
                CONNECTING.set(false);
            } else if (handler.failed()) {
                promise.fail(handler.cause());
                CONNECTING.set(false);
            }
        });
        return promise.future();
    }

    private Future<RedisConnection> createNormalRedisClient() {
        Promise<RedisConnection> promise = Promise.promise();
        RedisOptions redisOptions = new RedisOptions()
                .addConnectionString(nacosConfig.getProperty("redis.host"))
                .setType(RedisClientType.STANDALONE);
        log.info("redis NetClientOptions is {}" ,redisOptions.getNetClientOptions().toJson());
        if (redis != null) {
            redis.close();;
        }
        if (CONNECTING.compareAndSet(false, true)) {
            redis = Redis.createClient(vertx,redisOptions);
            redis.connect(handler->{
                if (handler.succeeded()) {
                    RedisConnection client = handler.result();
                    promise.complete(client);
                    CONNECTING.set(false);
                } else if (handler.failed()) {
                    promise.fail(handler.cause());
                    CONNECTING.set(false);
                }
            });
        } else {
            promise.complete();
        }
        return promise.future();
    }

    /**
     * Attempt to reconnect up to MAX_RECONNECT_RETRIES
     */
    private void attemptReconnect(int retry) {
        if (retry > MAX_RECONNECT_RETRIES) {
            CONNECTING.set(false);
        } else {
            // retry with backoff up to 10240 ms
            long backoff = (long) (Math.pow(2, Math.min(retry, 10)) * 10);

            vertx.setTimer(backoff, timer -> {
                createNormalRedisClient()
                    .onFailure(t -> attemptReconnect(retry + 1));
            });
        }
    }
}
