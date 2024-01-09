package com.tdj.datacenter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.tdj.common.BaseVerticle;
import com.tdj.common.constant.EventBusAddressConstant;
import com.tdj.common.dingding.DingDingApi;
import com.tdj.common.dingding.DingDingApiNew;
import com.tdj.common.domain.RedisEvent;
import com.tdj.common.domain.Result;
import com.tdj.common.utils.FeignUtils;
import com.tdj.common.utils.RedisUtils;
import com.tdj.datacenter.component.TestCompontent;
import com.tdj.datacenter.dao.MyTestDao;
import com.tdj.datacenter.dao.pojo.Test;
import com.tdj.datacenter.domain.EntUser;
import com.tdj.datacenter.domain.Oauth2Token;
import com.tdj.datacenter.domain.StockConfig;
import com.tdj.datacenter.feign.PlatformService;
import com.tdj.datacenter.handler.CheckHandler;
import com.tdj.jooq.tables.pojos.CwSum;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ApiVerticle  extends BaseVerticle {
    private DingDingApi dingDingApi = new DingDingApi();
    private DingDingApiNew dingDingApiNew = new DingDingApiNew();
    private FeignUtils feignUtils = new FeignUtils();
    private CheckHandler checkHandler = new CheckHandler();
    private RedisUtils redisUtils;
    private MyTestDao myTestDao;
    private TestCompontent testCompontent;

    @Override
    public void doInit() {
        // 创建 HTTP 服务器
        HttpServer server = vertx.createHttpServer();

        // 创建路由
        Router router = Router.router(vertx);

        // 处理 /api/hello 请求
        router.get("/datacenter/fetch/userid").handler(this::fetchUserId);
        router.get("/datacenter/sendtome").handler(this::sendToMe);
        router.get("/datacenter/fetchstock").handler(this::fetchStock);
        router.get("/datacenter/test").handler(this::test);
        router.post("/datacenter/setloglevel").handler(this::configLogLevel);
        router.post("/datacenter/feign").handler(this::feignTest);
        router.post("/datacenter/redis/psubscribe_key_expired/").handler(this::redisPsubscribeKeyExpired);
        router.post("/datacenter/redis/set/").handler(this::redisSet);
        router.post("/datacenter/redis/get/").handler(this::redisGet);
        router.post("/datacenter/redis/setnx/").handler(this::redisSetNx);
        router.post("/datacenter/redis/keys/").handler(this::redisKeys);
        router.post("/datacenter/redis/getset/").handler(this::redisGetSet);
        router.post("/datacenter/redis/getlock/").handler(this::redisGetLock);
        router.post("/datacenter/redis/dellock/").handler(this::redisDelLock);
        router.post("/datacenter/redis/eval/").handler(this::redisEval);
        router.post("/datacenter/redis/event/").handler(this::redisEvent);
        router.post("/datacenter/mysql/select/").handler(this::mysqlSelect);
        router.post("/datacenter/mysql/dotest/").handler(this::mysqlDoTest);
        router.post("/datacenter/mysql/insert/").handler(this::mysqlInsert);
        router.post("/datacenter/component/test/").handler(this::componentTest);

        // 将路由与服务器关联
        server.requestHandler(router);

        int port = config().getInteger("server.port");
        // 启动服务器
        server.listen(port, ar -> {
            if (ar.succeeded()) {
                log.info("Server started on port {}",port);
            } else {
                System.out.println("Server failed to start");
            }
        });
    }

    private void test(RoutingContext routingContext) {
        log.info("this is a test url");
        routingContext.response().putHeader("content-type", "text/plain");
        routingContext.response().end("this is a test url");
    }

    private void fetchUserId(RoutingContext routingContext) {
        vertx.executeBlocking((Promise<String> future) -> {
            // 长时间运行的操作，例如调用 dingDingApi
            String userId = dingDingApi.toGetDingDingPeople("18971490808");
            // 通知 Future 操作已完成
            future.complete(userId);
        }, result -> {
            if (result.succeeded()) {
                // 在事件循环线程中继续处理响应
                String responseMessage = result.result();
                routingContext.response().putHeader("content-type", "text/plain");
                routingContext.response().end(responseMessage);
            } else {
                // 处理错误
                routingContext.fail(result.cause());
            }
        });
    }

    private void sendToMe(RoutingContext routingContext) {
        vertx.executeBlocking((Promise<String> future) -> {
            // 长时间运行的操作，例如调用 dingDingApi
            List<String> userIds = new ArrayList<String>();
            userIds.add("manager2769");
            try {
                JsonObject message = new JsonObject();
                message.put("content","推送测试");
                dingDingApiNew.batchSendTo(userIds,"sampleText",message.toString());
                // 通知 Future 操作已完成
                future.complete();
            } catch (Exception e) {
                future.fail(e);
            }
        }, result -> {
            if (result.succeeded()) {
                // 在事件循环线程中继续处理响应
                String responseMessage = "send successed!";
                routingContext.response().putHeader("content-type", "text/plain");
                routingContext.response().end(responseMessage);
            } else {
                // 处理错误
                routingContext.fail(result.cause());
            }
        });
    }

    private void fetchStock(RoutingContext routingContext) {
        Future<List<StockConfig>> future = checkHandler.fetchStock(vertx,nacosConfig);
        future.onComplete(handler->{
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            StringBuilder sb = new StringBuilder();
            sb.append("当前").append(sdf.format(new Date())).append("关注的股票列表价格如下").append("<br>");
            for (StockConfig stockConfig:handler.result()) {
                sb.append(stockConfig.getName()).append(":").append(stockConfig.getNow()).append("<br>");
            }
            List<String> userIds = new ArrayList<String>();
            userIds.add("manager2769");
            JsonObject json = new JsonObject();
            json.put("title","股票价格推送");
            json.put("text",sb.toString());
            json.put("singleTitle","查询价格");
            json.put("singleURL","http://hw.zhengyakeji.com:10360/api/fetchstock");
            dingDingApiNew.batchSendTo(vertx,userIds,"sampleActionCard",json.toString());
            String responseMessage = "send successed!";
            routingContext.response().putHeader("content-type", "text/plain");
            routingContext.response().end(responseMessage);
        }).onFailure(err->{
            routingContext.fail(err.getCause());
        });
    }

    private void configLogLevel(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();

        // 读取请求体中的 JSON 数据
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            log.info("Received JSON: " + json.encodePrettily());
            String level = json.getString("level");
            Logger logger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
            switch (level) {
                case "error":{
                    logger.setLevel(Level.ERROR);
                    break;
                }
                case "warn":{
                    logger.setLevel(Level.WARN);
                    break;
                }
                case "info":{
                    logger.setLevel(Level.INFO);
                    break;
                }
                case "debug":{
                    logger.setLevel(Level.DEBUG);
                    break;
                }
                case "trace":{
                    logger.setLevel(Level.TRACE);
                    break;
                }
            }
            log.error("test error");
            log.warn("test warn");
            log.info("test info");
            log.debug("test debug");
            log.trace("test trace");
            // 发送响应
            routingContext.response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("status", "ok").encode());
        });
    }

    private void feignTest(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            EntUser entUser = json.mapTo(EntUser.class);
            Future<PlatformService> clientFuture = feignUtils.getFeignClient(vertx,config(),PlatformService.class);
            clientFuture.onComplete(handler ->{
                Future<Result<Oauth2Token>> loginInfo = handler.result().doEntLogin(entUser);
                /* Then */
                loginInfo.onComplete(res -> {
                    if (res.succeeded() && res.result().getStatus() == 1) {
                        routingContext.response()
                                .putHeader("content-type", "application/json")
                                .end(JsonObject.mapFrom(res.result()).toString());
                    } else {
                        log.info("",res.cause());
                        routingContext.response().putHeader("content-type", "text/plain");
                        routingContext.response().end(res.cause().getMessage());
                    }
                });
            }).onFailure(err->{
                log.info("",err);
                routingContext.response().putHeader("content-type", "text/plain");
                routingContext.response().end(err.getMessage());
            });
        });
    }

    private void redisPsubscribeKeyExpired(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            String key = json.getString("key");
            String value = json.getString("value");
            long interval = json.getLong("interval");//秒
            long offset = json.getLong("offset");//秒
            Future<Void> setExFuture = redisUtils.setEx(key, interval, value);
            Future<String> psubscribeKeyExpiredFuture = redisUtils.psubscribeKeyExpired(key, interval * 1000);
            setExFuture.onComplete(handler -> {
                vertx.executeBlocking(new Callable() {
                    @Override
                    public Object call() throws Exception {
                        try {
                            Thread.sleep(offset * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        redisUtils.del(key);
                        return null;
                    }
                });
            });
            psubscribeKeyExpiredFuture.onComplete(message -> {
                if (message.succeeded()) {
                    log.info("{} is expired,to do something here!", message.result());
                    String responseMessage = "检测到" + message.result() + "的超时事件";
                    routingContext.response().putHeader("content-type", "text/plain");
                    routingContext.response().end(responseMessage);
                } else {
                    log.error("redisPsubscribeKeyExpired error>", message.cause());
                    routingContext.response().putHeader("content-type", "text/plain");
                    routingContext.response().end(message.cause().getMessage());
                }
            });
        });
    }

    private void redisSetNx(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            String key = json.getString("key");
            String value = json.getString("value");
            Future<Boolean> future = redisUtils.setNx(key, value);
            future.onComplete(handler->{
                if (handler.succeeded()) {
                    routingContext.response().putHeader("content-type", "text/plain");
                    routingContext.response().end(handler.result().toString());
                } else {
                    log.error("",handler.cause());
                    routingContext.response().putHeader("content-type", "text/plain");
                    routingContext.response().end(handler.cause().getMessage());
                }
            });
        });
    }

    private void redisGetSet(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            String key = json.getString("key");
            String value = json.getString("value");
            Future<String> future = redisUtils.getSet(key, value);
            future.onComplete(handler->{
                if (handler.succeeded()) {
                    if (handler.result() == null) {
                        routingContext.response().putHeader("content-type", "text/plain");
                        routingContext.response().end("不存在的key");
                    } else {
                        routingContext.response().putHeader("content-type", "text/plain");
                        routingContext.response().end(handler.result());
                    }
                } else {
                    log.error("",handler.cause());
                    routingContext.response().putHeader("content-type", "text/plain");
                    routingContext.response().end(handler.cause().getMessage());
                }
            });
        });
    }

    private void redisGet(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            String key = json.getString("key");
            String value = json.getString("value");
            Future<String> future = redisUtils.get(key);
            future.onComplete(handler->{
                if (handler.succeeded()) {
                    if (handler.result() == null) {
                        routingContext.response().putHeader("content-type", "text/plain");
                        routingContext.response().end("不存在的key");
                    } else {
                        routingContext.response().putHeader("content-type", "text/plain");
                        routingContext.response().end(handler.result());
                    }
                } else {
                    log.error("",handler.cause());
                    routingContext.response().putHeader("content-type", "text/plain");
                    routingContext.response().end(handler.cause().getMessage());
                }
            });
        });
    }

    private void redisSet(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            String key = json.getString("key");
            String value = json.getString("value");
            redisUtils.set(key,value);
            routingContext.response().putHeader("content-type", "text/plain");
            routingContext.response().end("OK");
        });
    }

    private void redisKeys(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            String key = json.getString("key");
            String value = json.getString("value");
            Future<JsonArray> future = redisUtils.keys(key);
            future.onComplete(handler->{
                if (handler.succeeded()) {
                    routingContext.response().putHeader("content-type", "text/plain");
                    routingContext.response().end(handler.result().toString());
                } else {
                    log.error("",handler.cause());
                    routingContext.response().putHeader("content-type", "text/plain");
                    routingContext.response().end(handler.cause().getMessage());
                }
            });
        });
    }

    private void redisGetLock(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            String key = json.getString("key");
            long expire = json.getLong("expire");//秒
            Future<Boolean> future = redisUtils.getLock(key,expire * 1000);
            future.compose(handler->{
                if (!handler) {
//                    log.info("删除锁后重新获取锁");
//                    return redisUtils.delLock(vertx,key).compose(t-> redisUtils.getLock(key,expire * 1000));
                    return Future.succeededFuture(false);
                } else {
                    return Future.succeededFuture(true);
                }
            }).onComplete(handler->{
                routingContext.response().putHeader("content-type", "text/plain");
                routingContext.response().end(handler.result().toString());
            }).onFailure(err->{
                log.error("",err.getCause());
                routingContext.response().putHeader("content-type", "text/plain");
                routingContext.response().end(err.getMessage());
            });
        });
    }

    private void redisDelLock(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            String key = json.getString("key");
            redisUtils.delLock(key);
            routingContext.response().putHeader("content-type", "text/plain");
            routingContext.response().end("OK");
        });
    }
    private void redisEval(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            String key = json.getString("key");
            Long expire = json.getLong("expire");
            Long timeout = json.getLong("timeout");
            Boolean forceRenew = json.getBoolean("force_renew");
            Boolean ordered = json.getBoolean("ordered");

            //eval test
//            TimeUnit unit = TimeUnit.HOURS;
//            List<String> args = new ArrayList<>();
//            args.add(RedisScriptsConstants.SERIAL_NUMBER);
//            args.add("1");
//            args.add(RedisKeyConstants.SERIAL_NUMBER_PREFIX);
//            args.add("" + unit.toMillis(expire));
//            log.info("eval args:{}" ,JsonArray.of(args));
//            Future<String> future = redisUtils.eval(args);
//            future.onComplete(handler->{
//                routingContext.response().putHeader("content-type", "text/plain");
//                routingContext.response().end(handler.result().toString());
//            }).onFailure(err->{
//                log.error("",err.getCause());
//                routingContext.response().putHeader("content-type", "text/plain");
//                routingContext.response().end(err.getMessage());
//            });

            //lockReentrantV2 test
            Context context = redisUtils.getTransactionContext(false);
            log.info(">>>>context_str1:{}",context);
            redisUtils.lockReentrantV2(context, key,expire, TimeUnit.SECONDS,timeout).compose(lock -> {
                if (lock) {
                    return redisUtils.get("abc");
                } else {
                    log.info(">>>>str1 is locked");
                    return Future.failedFuture("");
                }
            }).compose(str ->{
                log.info(">>>>str1:{}",str);
//                Context otherContext = redisUtils.getTransactionContext(forceRenew);
//                log.info(">>>>context_str2:{}",otherContext);
                return redisUtils.lockReentrantV2(context, key,expire, TimeUnit.SECONDS,timeout).compose(lock->{
                    if (lock) {
                        return redisUtils.get("abcd");
                    } else {
                        log.info(">>>>str2 is locked");
                        return Future.failedFuture("");
                    }
                }).onSuccess(str2->{
                    log.info(">>>>unlock str2:{}",str2);
                    redisUtils.unlockReentrant(context,key);
                }).onFailure(err->{
                    log.info(">>>>unlock err2:{}",err.getMessage());
                    redisUtils.unlockReentrant(context, key);
                });
            }).onSuccess(b ->{
                redisUtils.unlockReentrant(context,key).onSuccess(b2->{
                    vertx.executeBlocking((Callable<Void>) ()->{
                        try {
                            Context otherContext = redisUtils.getTransactionContext(forceRenew);
                            log.info(">>>>context_str3:{}",otherContext);
                            redisUtils.lockReentrantV2(otherContext, key,expire, TimeUnit.SECONDS,timeout).compose(lock -> {
                                if (lock) {
                                    return redisUtils.get("abc");
                                } else {
                                    log.info(">>>>str3 is locked");
                                    return Future.failedFuture("");
                                }
                            }).compose(str ->{
                                log.info(">>>>str3:{}",str);
                                return redisUtils.lockReentrantV2(otherContext, key,expire, TimeUnit.SECONDS,timeout);
                            }).compose(lock->{
                                if (lock) {
                                    return redisUtils.get("abcd");
                                } else {
                                    log.info(">>>>str4 is locked");
                                    return Future.failedFuture("");
                                }
                            }).onComplete(handler->{
                                redisUtils.unlockReentrant(otherContext,key);
                                redisUtils.unlockReentrant(otherContext, key);
                            }).onSuccess(str ->{
                                log.info(">>>>str4:{}",str);
                            }).onFailure(err->{
                                log.info(">>>>err2:{}",err.getMessage());
                            });
                            routingContext.response().putHeader("content-type", "text/plain");
                            routingContext.response().end("OK");
                        } catch (Exception e) {
                            log.error("",e);
                        }
                        return null;
                    },ordered);
                });
            }).onFailure(err->{
                log.info(">>>>unlock err1:{}",err.getMessage());
                redisUtils.unlockReentrant(context, key);
            });
        });
    }

    private void redisEvent(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            String key = json.getString("key");
            final MessageConsumer<RedisEvent<String>> messageConsumer = vertx.eventBus().<RedisEvent<String>>consumer(EventBusAddressConstant.REDIS_KEY_EXPIRE_EVENT);
            messageConsumer.handler((Message<RedisEvent<String>> message) -> {
                log.info("收取到过期的事件:" + message.body().getEvent() + "; Thread: " + Thread.currentThread().toString());
                log.info("过期的key="+message.body().getKey());
                if (message.body().getEvent().equalsIgnoreCase("__keyevent@" + nacosConfig.getProperty("redisson.db") + "__:expired")
                    && message.body().getKey().equalsIgnoreCase(key)) {
                    log.info("监听到一次{}的过期事件,不会再监听第二次",key);
                    messageConsumer.unregister();
                }
            });
            final MessageConsumer<RedisEvent<String>> messageConsumer2 = vertx.eventBus().<RedisEvent<String>>consumer(EventBusAddressConstant.REDIS_KEY_EXPIRE_EVENT);
            messageConsumer2.handler((Message<RedisEvent<String>> message) -> {
                log.info("收取到过期的事件2:" + message.body().getEvent() + "; Thread: " + Thread.currentThread().toString());
                log.info("过期的key2="+message.body().getKey());
                if (message.body().getEvent().equalsIgnoreCase("__keyevent@" + nacosConfig.getProperty("redisson.db") + "__:expired")
                    && message.body().getKey().equalsIgnoreCase(key)) {
                    log.info("监听到一次{}的过期事件2,不会再监听第二次",key);
                    messageConsumer2.unregister();
                }
            });
            routingContext.response().putHeader("content-type", "text/plain");
            routingContext.response().end("OK");
        });
    }

    private void mysqlDoTest(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            myTestDao.doTest();
            routingContext.response().putHeader("content-type", "text/plain");
            routingContext.response().end("OK");
        });
    }
    private void mysqlSelect(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            try {
                JsonObject json = buffer.toJsonObject();
                String sql = json.getString("sql");
                JsonObject param1 = json.getJsonObject("param");
                Test t = new Test();
                t.setId(param1.getLong("id"));
                Future<List<CwSum>> future = myTestDao.myFirstSelect();
                future.onSuccess(cwSums->{
                    for (CwSum test : cwSums) {
                        log.info(test.toString());
                    }
                    routingContext.response().putHeader("content-type", "text/plain");
                    routingContext.response().end("OK");
                });
            }catch (Exception e) {
                routingContext.response().putHeader("content-type", "text/plain");
                routingContext.response().end("ERROR");
            }
        });
    }
    private void mysqlInsert(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            try {
                JsonObject json = buffer.toJsonObject();
                String sql = json.getString("sql");
                JsonObject param1 = json.getJsonObject("param");
                CwSum cwSum = param1.mapTo(CwSum.class);

                myTestDao.myInsert(cwSum).compose(row->{
                    return  myTestDao.myFirstSelect();
                }).onSuccess(cwSums->{
                    for (CwSum test : cwSums) {
                        log.info(test.toString());
                    }
                    routingContext.response().putHeader("content-type", "text/plain");
                    routingContext.response().end("OK");
                });
            }catch (Exception e) {
                log.error("",e);
                routingContext.response().putHeader("content-type", "text/plain");
                routingContext.response().end("ERROR");
            }
        });
    }
    private void componentTest(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            try {
                JsonObject json = buffer.toJsonObject();
                testCompontent.doTest();
                routingContext.response().putHeader("content-type", "text/plain");
                routingContext.response().end("OK");
            }catch (Exception e) {
                routingContext.response().putHeader("content-type", "text/plain");
                routingContext.response().end("ERROR");
            }
        });
    }
}