package com.tdj.common.verticle;

import com.tdj.common.BaseVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.*;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class RedisVerticle extends BaseVerticle {
    private static final int MAX_RECONNECT_RETRIES = 16;
    private RedisAPI api;


    @Override
    protected void doInit() {
        super.doInit();
        eventBus.consumer("redis.set", this::set);
        eventBus.consumer("redis.setex", this::setEx);
        eventBus.consumer("redis.setnx", this::setNx);
        eventBus.consumer("redis.get", this::get);
        eventBus.consumer("redis.getset", this::getSet);
        eventBus.consumer("redis.del", this::del);
        eventBus.consumer("redis.keys", this::keys);
        eventBus.consumer("redis.psubscribe_key_expired", this::psubscribeKeyExpired);
        initMain();
    }

    private void initMain() {
        Future<RedisConnection> future = createRedisClient();
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
        }).onFailure(err->{
            log.error("redis 初始化异常！");
        });
    }

    private void set(Message<JsonObject> message) {
        JsonObject body = message.body();
        String key = body.getString("key");
        String value = body.getString("value");
        List<String> args = new ArrayList<>();
        args.add(key);
        args.add(value);
        api.set(args,handler->{
            if (handler.succeeded()) {
                log.debug("set({},{}) success",key,value);
                message.reply("OK");
            } else {
                log.error("set({},{}) faild.return is",key,value,handler.cause());
                message.fail(-1,"set error.");
            }
        });
    }

    private void get(Message<String> message) {
        api.get(message.body(),handler->{
            if (handler.succeeded()) {
                if (handler.result() == null) {
                    message.reply(null);
                } else {
                    message.reply(handler.result().toString());
                }
            } else {
                log.error("get({}) faild",message.body(),handler.cause());
                message.fail(-1,"set error.");
            }
        });
    }

    private void getSet(Message<JsonObject> message) {
        JsonObject body = message.body();
        String key = body.getString("key");
        String value = body.getString("value");
        api.getset(key,value,handler->{
            if (handler.succeeded()) {
                if (handler.result() == null) {
                    message.reply(null);
                } else {
                    message.reply(handler.result().toString());
                }
            } else {
                message.fail(-1,"set error.");
            }
        });
    }

    private void del(Message<String> message) {
        List<String> args = new ArrayList<>();
        args.add(message.body());
        api.del(args,handler->{
            if (handler.succeeded()) {
                log.debug("del({}) success",message.body());
                message.reply("OK");
            } else {
                log.error("del({}) faild.",message.body(),handler.cause());
                message.fail(-1,"set error.");
            }
        });
    }

    private void keys(Message<String> message) {
        api.keys(message.body(), (AsyncResult<Response> handler) ->{
            if (handler.succeeded()) {
                try {
                    Response response = handler.result();
                    JsonArray reply = response.stream()
                            .map(Object::toString)
                            .collect(JsonArray::new,JsonArray::add,JsonArray::addAll);
                    message.reply(reply);
                }catch (Exception e) {
                    log.error("",e);
                }
            } else {
                message.fail(-1,"kyes error.");
            }
        });
    }

    private void setEx(Message<JsonObject> message) {
        JsonObject body = message.body();
        String key = body.getString("key");
        String value = body.getString("value");
        long interval = body.getLong("interval");
        api.setex(key,""+interval,value,handler->{
            if (handler.succeeded()) {
                log.debug("setKeyExpired({},{},{}) success",key,value,interval);
                message.reply("OK");
            } else {
                log.error("setKeyExpired({},{},{}) faild.return is",key,value,interval,handler.cause());
                message.fail(-1,"setex error.");
            }
        });
    }

    private void setNx(Message<JsonObject> message) {
        JsonObject body = message.body();
        String key = body.getString("key");
        String value = body.getString("value");
        List<String> args = new ArrayList<>();
        args.add(key);
        args.add(value);
        api.setnx(key,value,response->{
            if (response.succeeded()) {
                message.reply(response.result().toString().equalsIgnoreCase("1"));
            } else {
                message.fail(-1,"set error.");
            }
        });
    }

    private void psubscribeKeyExpired(Message<JsonObject> message) {
        JsonObject body = message.body();
        String key = body.getString("key");
        long interval = body.getLong("interval") * 1000;
        Future<RedisConnection> future = createRedisClient();
        future.onComplete(complet->{
            final long timerId;
            RedisConnection client = future.result();
            //timer设置为interval + 10000 是为了避免边界异常
            timerId = vertx.setTimer(interval + 10000,timer->{
                //未监听到超时事件，无需callback业务端，直接结束本次的连接
                log.debug("没有在间隔{}内收到{}的超时事件,主动关闭此次redis连接",interval,key);
                client.close();
            });
            client.handler(resopnse -> {
                log.info(resopnse.toString());
                if (resopnse.size() == 4
                        && resopnse.get(0).toString().equalsIgnoreCase("pmessage")
                        && resopnse.get(1).toString().equalsIgnoreCase("__keyevent@*__:expired")
                        && resopnse.get(3).toString().equalsIgnoreCase(key)) {
                    //收到了过期事件，需要callback业务端处理此事件
                    message.reply(body);
                    log.debug("监听到{}的超时事件，取消边界检查器",interval);
                    vertx.cancelTimer(timerId);
                    client.close();
                }
            });
            RedisAPI api = RedisAPI.api(client);
            List<String> args = new ArrayList<>();
            args.add("__keyevent@*__:expired");
            api.psubscribe(args, handler->{
                if (handler.succeeded()) {
                    log.debug("psubscribe 设置成功");
                }
            });
        }).onFailure(err->{
            message.fail(-1,"建立redis连接失败");
        });
    }

    private Future<RedisConnection> createRedisClient() {
        Promise<RedisConnection> promise = Promise.promise();
        RedisOptions redisOptions = new RedisOptions()
                .addConnectionString(nacosConfig.getProperty("redis.host"))
                .setType(RedisClientType.STANDALONE);
        Redis redis = Redis.createClient(vertx,redisOptions);
        redis.connect(handler->{
            if (handler.succeeded()) {
                RedisConnection client = handler.result();
                promise.complete(client);
            } else if (handler.failed()) {
                promise.fail(handler.cause());
            }
        });
        return promise.future();
    }

    /**
     * Attempt to reconnect up to MAX_RECONNECT_RETRIES
     */
    private void attemptReconnect(int retry) {
        if (retry < MAX_RECONNECT_RETRIES) {
            // retry with backoff up to 10240 ms
            long backoff = (long) (Math.pow(2, Math.min(retry, 10)) * 10);

            vertx.setTimer(backoff, timer -> {
                createRedisClient().onFailure(t -> {
                    attemptReconnect(retry + 1);
                });
            });
        }
    }
}
