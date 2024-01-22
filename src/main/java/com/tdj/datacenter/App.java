package com.tdj.datacenter;

import com.jay.common.Context;
import com.jay.common.MainLancher;
import com.jay.common.verticle.JackonConfigVerticle;
import com.jay.common.verticle.NacosVerticle;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {
    public static void main(String[] args) {
        Context.setVertxInstance(Vertx.vertx());
        ConfigRetrieverOptions options = new ConfigRetrieverOptions()
                .addStore(new ConfigStoreOptions()
                        .setType("file")
                        .setFormat("properties")
                        .setOptional(true)
                        .setConfig(new JsonObject().put("path", "config.properties"))
                );
        ConfigRetriever retriever = ConfigRetriever.create(Context.getVertxInstance(), options);
        retriever.getConfig(ar -> {
            if (ar.succeeded()) {
                DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(ar.result());
                Context.getVertxInstance().deployVerticle(new ApiVerticle(),deploymentOptions);
                Context.getVertxInstance().deployVerticle(new SubstituteSignoVerticle(),deploymentOptions);
                Context.getVertxInstance().deployVerticle(new MainLancher(),deploymentOptions);
                Context.getVertxInstance().deployVerticle(new JackonConfigVerticle(),deploymentOptions);
                Context.getVertxInstance().deployVerticle(new NacosVerticle(),deploymentOptions);
            }
        });
    }
}
