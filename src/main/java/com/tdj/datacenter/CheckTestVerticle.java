package com.tdj.datacenter;

import com.tdj.datacenter.handler.CheckHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.util.Properties;

@Slf4j
public class CheckTestVerticle extends AbstractVerticle {
    private Properties properties = new Properties();
    private CheckHandler checkHandler = new CheckHandler();

    @Override
    public void start(Promise<Void> startPromise) {
        initStockConfig();
        vertx.setPeriodic(10000,timeId -> {
            LocalTime currentTime = LocalTime.now(); // 获取当前时间
            LocalTime startTime = LocalTime.of(9, 30); // 开始时间
            LocalTime endTime = LocalTime.of(11, 30); // 结束时间
            LocalTime nextStartTime = LocalTime.of(13, 0); // 下一个开始时间
            LocalTime nextEndTime = LocalTime.of(15, 0); // 下一个结束时间

            if (currentTime.isAfter(startTime) && currentTime.isBefore(endTime)) {
                checkHandler.doCheck(vertx, properties);
            } else if (currentTime.isAfter(nextStartTime) && currentTime.isBefore(nextEndTime)) {
                checkHandler.doCheck(vertx, properties);
            } else {
                checkHandler.doCheck(vertx, properties);
            }
        });
    }

    private void initStockConfig() {
        properties.put("list","sz300079,sz300201");
        properties.put("sz300079_n","数码视讯");
        properties.put("sz300079_l","6.2");
        properties.put("sz300079_h","6.2");
        properties.put("sz300201_n","海伦哲");
        properties.put("sz300201_l","4.5");
        properties.put("sz300201_h","5.5");
    }
}