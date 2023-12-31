package com.tdj.datacenter;

import com.tdj.common.BaseVerticle;
import com.tdj.datacenter.handler.CheckHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringReader;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Properties;
import java.util.TimeZone;

@Slf4j
public class CheckVerticle extends BaseVerticle {
    private EventBus eventBus;
    private Properties nacosConfig;
    private CheckHandler checkHandler = new CheckHandler();

    @Override
    public void doInit() {
        TimeZone timeZone = TimeZone.getDefault();
        log.info("Default TimeZone: " + timeZone.getDisplayName());
        TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
//        vertx.eventBus().consumer("check_cmd", (Message<Properties> config) -> {
//            checkHandler.doCheck(vertx);
//        });
        vertx.setPeriodic(10000,timeId -> {
            LocalTime currentTime = LocalTime.now(ZoneId.of("Asia/Shanghai")); // 获取当前时间
            LocalTime startTime = LocalTime.of(9, 30); // 开始时间
            LocalTime endTime = LocalTime.of(11, 30); // 结束时间
            LocalTime nextStartTime = LocalTime.of(13, 0); // 下一个开始时间
            LocalTime nextEndTime = LocalTime.of(15, 0); // 下一个结束时间

            if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)) {
                checkHandler.doCheck(vertx,nacosConfig);
            } else if (currentTime.isAfter(nextStartTime) && currentTime.isBefore(nextEndTime)) {
                checkHandler.doCheck(vertx,nacosConfig);
            }
        });
    }
}