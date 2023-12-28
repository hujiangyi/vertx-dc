package com.tdj.datacenter;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

public class App {
    private static Vertx vertx;
    public static Vertx getVertxInstance(){
        return vertx;
    }
    public static void main(String[] args) {
        vertx = Vertx.vertx();
        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
            .addStore(new ConfigStoreOptions()
                .setType("file")
                .setFormat("properties")
                .setOptional(true)
                .setConfig(new JsonObject().put("path", "config.properties"))
            );
        ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
        retriever.getConfig(ar -> {
            if (ar.failed()) {
                // 处理获取配置失败的情况
            } else {
                DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(ar.result());
                vertx.deployVerticle(new NacosVerticle(),deploymentOptions);
                vertx.deployVerticle(new CheckVerticle(),deploymentOptions);
                vertx.deployVerticle(new ApiVerticle(),deploymentOptions);
//                vertx.deployVerticle(new ScheduleVerticle(),deploymentOptions);
//                vertx.deployVerticle(new CheckTestVerticle(),deploymentOptions);
            }
        });
    }
}
