package com.tdj.common.verticle;

import cn.hutool.core.net.NetUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.tdj.datacenter.App;
import io.vertx.core.AbstractVerticle;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Executor;

@Slf4j
public class NacosVerticle extends AbstractVerticle {
    private ConfigService configService;

    @Override
    public void start() throws Exception {
        // 从配置文件中获取Nacos配置中心的地址和命名空间
        String serverAddr = config().getString("nacos.server-addr");
        String namespace = config().getString("nacos.namespace");
        String serviceName = config().getString("nacos.service-name");
        int port = config().getInteger("server.port");
        String subnet = config().getString("tianyuan.preferred-networks");

        //服务发现注册
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, serverAddr);
        NamingService namingService = NacosFactory.createNamingService(properties);
        String ip = "127.0.0.1";
        Set<String> ipAddresses = NetUtil.localIpv4s();
        for (String ipAddress : ipAddresses) {
            if (ipAddress.startsWith(subnet)) {
                ip = ipAddress;
                break;
            }
        }
        Instance instance = new Instance();
        instance.setIp(ip);
        instance.setPort(port);
        instance.setServiceName(serviceName);
        namingService.registerInstance(serviceName, instance);

        //配置中心注册
        properties = new Properties();
//        properties.setProperty(PropertyKeyConst.NAMESPACE,"xxx-xx-xxxx");
//        //如果开启了Nacos权限校验，设置用户名
//        properties.setProperty(PropertyKeyConst.USERNAME,"nacos");
//        properties.setProperty(PropertyKeyConst.PASSWORD,"nacos");
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, serverAddr);
        properties.setProperty(PropertyKeyConst.CONFIG_LONG_POLL_TIMEOUT,"3000");
        properties.setProperty(PropertyKeyConst.CONFIG_RETRY_TIME,"5");
        properties.setProperty(PropertyKeyConst.MAX_RETRY,"5");

        // 创建Nacos配置中心的连接
        configService = NacosFactory.createConfigService(properties);

        // 订阅Nacos配置中心的配置变化
        String dataId = config().getString("nacos.data-id");
        String group = config().getString("nacos.group");
        configService.addListener(dataId, group, new Listener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                try {
                    vertx.eventBus().publish("nacos.config_renew", configInfo);
                } catch (Exception e) {
                    log.debug("读取配置失败 dataId[{}],group[{}]",dataId, group,e);
                }
            }
        });
        String configInfo = configService.getConfig(dataId, group, 5000);
        App.initModule(vertx,configInfo);
        vertx.eventBus().publish("nacos.config_init_success", configInfo);
    }

    @Override
    public void stop() throws Exception {
        // 关闭Nacos配置中心的连接
        configService.shutDown();
    }
}
