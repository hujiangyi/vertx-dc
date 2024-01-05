package com.tdj.common.redisson;

import io.vertx.core.Context;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonLock;
import org.redisson.command.CommandAsyncExecutor;

@Slf4j
public class VertxRedissonLock extends RedissonLock {
    private Context context;
    public VertxRedissonLock(Context context, CommandAsyncExecutor commandExecutor, String name) {
        super(commandExecutor, name);
        this.context = context;
    }

    @Override
    protected String getLockName(long threadId) {
        if (context == null) {
            return super.getLockName(threadId);
        } else {
            return this.getServiceManager().getId() +"_" + context.getLocal("transaction_id");
        }
    }
}
