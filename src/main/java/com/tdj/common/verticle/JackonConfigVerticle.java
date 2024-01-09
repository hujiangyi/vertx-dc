package com.tdj.common.verticle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.jackson.DatabindCodec;

public class JackonConfigVerticle extends AbstractVerticle {

    @Override
    public void start() {
        ObjectMapper mapper = DatabindCodec.mapper();
        mapper.registerModule(new JavaTimeModule());
    }
}