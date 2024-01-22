package com.tdj.datacenter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import com.jay.common.BaseVerticle;
import com.jay.common.utils.RedisUtils;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;


@Slf4j
public class ApiVerticle extends BaseVerticle {
    private RedisUtils redisUtils;

    @Override
    public void after() {
        // 创建 HTTP 服务器
        HttpServer server = vertx.createHttpServer();

        // 创建路由
        Router router = Router.router(vertx);
        router.post("/datacenter/test").handler(this::test);
        router.post("/datacenter/setloglevel").handler(this::configLogLevel);
        server.requestHandler(router);

        int port = config().getInteger("server.port");
        server.listen(port, ar -> {
            if (ar.succeeded()) {
                log.info("Server started on port {}",port);
            } else {
                System.out.println("Server failed to start");
            }
        });
    }

    private void test(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            String responseMessage = "send successed!";
            routingContext.response().putHeader("content-type", "text/plain");
            routingContext.response().end(responseMessage);
        });
    }

    private void configLogLevel(RoutingContext routingContext) {
        HttpServerRequest request = routingContext.request();

        // 读取请求体中的 JSON 数据
        request.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            log.info("Received JSON: " + json.encodePrettily());
            String name = json.getString("name");
            String level = json.getString("level");
            Logger logger = (Logger) LoggerFactory.getLogger(name);
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
            // 发送响应
            routingContext.response()
                    .putHeader("content-type", "application/json")
                    .end(new JsonObject().put("status", "ok").encode());
        });
    }
}