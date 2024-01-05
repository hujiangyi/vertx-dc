package com.tdj.common.redisson;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class VertxRedisson extends Redisson {
    protected VertxRedisson(Config config) {
        super(config);
    }

    public static RedissonClient createVertxRedisson(Config config) {
        return new VertxRedisson(config);
    }

    public RLock getLock(Context context, String name) {
        return new VertxRedissonLock(context, this.commandExecutor, name);
    }
}
