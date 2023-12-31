package com.tdj.common;

import io.vertx.core.Future;
import io.vertx.core.Vertx;

import java.util.Properties;

public interface ModuleInit {
    Future<Boolean> init(Vertx vertx, Properties config);
}
