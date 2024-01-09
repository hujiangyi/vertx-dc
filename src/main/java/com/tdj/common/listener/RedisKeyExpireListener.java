package com.tdj.common.listener;

import com.tdj.common.constant.EventBusAddressConstant;
import com.tdj.common.domain.RedisEvent;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.listener.MessageListener;

/**
 * redis key过期事件监听器
 * 在这里处理过期订单的取消关单操作
 *
 * @author zhoubin
 * @date 2022/7/6
 */
@Slf4j
public class RedisKeyExpireListener implements MessageListener {
    private Vertx vertx;
    public RedisKeyExpireListener(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public void onMessage(CharSequence channel, Object msg) {
        //特别处理成send 是为了只有一个消费者响应此消息
        vertx.eventBus().publish(EventBusAddressConstant.REDIS_KEY_EXPIRE_EVENT,
                RedisEvent.<String>builder().event(channel.toString()).key(msg.toString()).build()
        );
    }
}
