package com.tdj.datacenter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.tdj.common.BaseVerticle;
import com.tdj.common.dingding.DingDingApi;
import com.tdj.common.dingding.DingDingApiNew;
import com.tdj.common.domain.Result;
import com.tdj.common.utils.FeignUtils;
import com.tdj.common.utils.RedisUtils;
import com.tdj.datacenter.domain.EntUser;
import com.tdj.datacenter.domain.Oauth2Token;
import com.tdj.datacenter.domain.StockConfig;
import com.tdj.datacenter.feign.PlatformService;
import com.tdj.datacenter.handler.CheckHandler;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.RedisConnection;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Slf4j
public class ApiVerticle  extends BaseVerticle {
    private DingDingApi dingDingApi = new DingDingApi();
    private DingDingApiNew dingDingApiNew = new DingDingApiNew();
    private FeignUtils feignUtils = new FeignUtils();
    private CheckHandler checkHandler = new CheckHandler();
    private RedisUtils redisUtils;

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
        router.get("/datacenter/redis/psubscribe_key_expired/").handler(this::redisPsubscribeKeyExpired);
        router.post("/datacenter/redis/set/").handler(this::redisSet);
        router.post("/datacenter/redis/get/").handler(this::redisGet);
        router.post("/datacenter/redis/setnx/").handler(this::redisSetNx);
        router.post("/datacenter/redis/keys/").handler(this::redisKeys);
        router.post("/datacenter/redis/getset/").handler(this::redisGetSet);

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
        String key = "abcdef";
        String value = "test";
        long interval = 5;
        redisUtils.setEx(vertx,key,interval,value);
//        redisUtils.del(vertx,key);
        Future<String> future = redisUtils.psubscribeKeyExpired(vertx,key,value,interval);
        future.onComplete(message ->{
            if (message.succeeded()) {
                log.info("{} is expired,to do something here!",message.result());
                String responseMessage = "检测到" + message.result() + "的超时事件";
                routingContext.response().putHeader("content-type", "text/plain");
                routingContext.response().end(responseMessage);
            } else {
                log.error("redisPsubscribeKeyExpired error>",message.cause());
                routingContext.response().putHeader("content-type", "text/plain");
                routingContext.response().end(message.cause().getMessage());
            }
        });
    }

    private void redisSetNx(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            String key = json.getString("key");
            String value = json.getString("value");
            Future<Boolean> future = redisUtils.setNx(vertx, key, value);
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
            Future<String> future = redisUtils.getSet(vertx, key, value);
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
            Future<String> future = redisUtils.get(vertx, key);
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
            redisUtils.set(vertx, key,value);
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
            Future<JsonArray> future = redisUtils.keys(vertx, key);
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
}