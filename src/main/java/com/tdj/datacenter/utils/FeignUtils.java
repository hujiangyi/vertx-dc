package com.tdj.datacenter.utils;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.tdj.datacenter.annotation.FeignService;
import feign.Request;
import feign.VertxFeign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class FeignUtils {
    public <T> Future<T> getFeignClient(Vertx vertx, JsonObject config, Class<T> tClass) {
        Promise<T> promise = Promise.promise();
        vertx.executeBlocking(handler -> {
            try {
                String serverAddr = config.getString("nacos.server-addr");
                Properties properties = new Properties();
                properties.setProperty(PropertyKeyConst.SERVER_ADDR, serverAddr);
                NamingService namingService = NacosFactory.createNamingService(properties);
                FeignService feignService = tClass.getAnnotation(FeignService.class);
                List<Instance> serviceInstances = namingService.getAllInstances(feignService.value());
                if (serviceInstances.size() == 0) {
                    promise.fail("找不到可用的服务提供者");
                }
                Instance instance = serviceInstances.get(0);
                T service = VertxFeign
                        .builder()
                        .vertx(vertx)
                        .decoder(new JacksonDecoder())
                        .encoder(new JacksonEncoder())
                        .options(new Request.Options(5L, TimeUnit.SECONDS, 5L, TimeUnit.SECONDS, true))
                        .target(tClass, "http://" + instance.getIp() + ":" + instance.getPort());
                promise.complete(service);
            } catch (Exception e) {
                promise.fail(e);
            }
        },promise);
        return promise.future();
    }
}
