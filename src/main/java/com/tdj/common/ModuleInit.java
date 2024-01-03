package com.tdj.common;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.Properties;

public interface ModuleInit {
    Future<Boolean> init(Vertx vertx, Properties nacosConfig, JsonObject config);
}
