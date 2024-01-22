package com.tdj.common.utils;

import ch.qos.logback.classic.Level;
import com.jay.common.ModuleInit;
import com.jay.common.annotation.Utils;
import com.jay.common.redisson.VertxRedisson;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisConnection;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.util.Properties;

@Utils
@Slf4j
public class RabbitmqUtils  implements ModuleInit {
    private Vertx vertx;
    private Properties nacosConfig;
    private JsonObject config;
    private RabbitMQClient rabbitMQClient;

    @Override
    public Future<Boolean> init(Vertx vertx, Properties properties, JsonObject jsonObject) {
        log.info("rabbitmq init.");
        Promise<Boolean> promise = Promise.promise();
        this.vertx = vertx;
        this.nacosConfig = nacosConfig;
        this.config = config;

        promise.complete(true);
        return promise.future();
    }
}
