package com.tdj.datacenter;

import com.jay.common.BaseVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.rabbitmq.QueueOptions;
import io.vertx.rabbitmq.RabbitMQClient;
import io.vertx.rabbitmq.RabbitMQOptions;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubstituteSignoVerticle extends BaseVerticle {
    private RabbitMQClient client;
    protected void after() {
        RabbitMQOptions rabbitMQOptions = new RabbitMQOptions();
        rabbitMQOptions.setHost(nacosConfig.getProperty("rabbitmq.host"));
        rabbitMQOptions.setPort(Integer.parseInt(nacosConfig.getProperty("rabbitmq.port")));
        rabbitMQOptions.setUser(nacosConfig.getProperty("rabbitmq.user"));
        rabbitMQOptions.setPassword(nacosConfig.getProperty("rabbitmq.password"));
        rabbitMQOptions.setVirtualHost(nacosConfig.getProperty("rabbitmq.virtualHost"));
        client = RabbitMQClient.create(vertx, rabbitMQOptions);
        client.start(startResult -> {
            if (startResult.succeeded()) {
                log.info("RabbitMQClient Connected");
                // 创建 替身队列 处理消息内容
                QueueOptions queueOptions = new QueueOptions();
                queueOptions.setAutoAck(true);
                client.queueDeclare(nacosConfig.getProperty("tianyuan.rabbitmq.queue.cmd"), true, false, false, handler->{});
                client.queueDeclare(nacosConfig.getProperty("tianyuan.rabbitmq.substitute.queue.cmd"), true, false, false, handler->{});
                client.basicConsumer(nacosConfig.getProperty("tianyuan.rabbitmq.substitute.queue.cmd")).compose(consumer->{
                    consumer.handler(handler->{
                        JsonObject json = new JsonObject(handler.body());
                        log.info("yuanshi:{}", json);
                        String topic = json.getString("topic");
//                        String payload = json.getString("payload");
                        if (topic.startsWith("/a1BkUFnh0eA/")) {
                            topic = topic.replaceAll("#.*/","/");
                            json.put("topic",topic);
                        } else {
                            json.put("topic",nacosConfig.getProperty("tianyuan.rabbitmq.substitute.topic"));
                        }
                        log.info("Substitute:{}", json);
                        client.basicPublish("", nacosConfig.getProperty("tianyuan.rabbitmq.queue.cmd"), json.toBuffer());
// TODO 以下代码是播报声音的格式分析 以及如何修改报表声音的例子代码 可以不用删除
                        //                        JsonObject body = new JsonObject(payload);
//                        String msgCmd = body.getString("cmd");
//                        if (msgCmd.equalsIgnoreCase("rs485")) {
//                            JsonArray rs485ch1_data = body.getJsonArray("rs485ch1_data");
//                            if (!rs485ch1_data.isEmpty()) {
//                                JsonObject dataMap0 = rs485ch1_data.getJsonObject(0);
//                                String data = dataMap0.getString("data");
//                                String f = data.substring(0, 12);
//                                if (f.equalsIgnoreCase("AA5501640022")) {
//                                    String length = data.substring(12,16);
//                                    String b = data.substring(16,34);
////                                    String crc = data.substring(34,38);
//                                    String af = data.substring(38,40);
//                                    log.info("f:{};length:{};b:{},af:{}", f, length, b, af);
//                                    b = b + CRC16Util.toHexStr("左边道闸1", "GBK");
//                                    //计算内容长度
//                                    length = StringUtils.toFixedStr(4, Integer.toHexString(b.length() / 2).toUpperCase());
//                                    //计算crc校验码
//                                    String cmd = f + length + b + "0000";
//                                    String crc = CRC16Util.getCrc(cmd.substring(4));
//                                    String newData = cmd.substring(0, cmd.length() - 4) + StringUtils.toFixedStr(4, crc) + "AF";
//                                    dataMap0.put("data", newData);
//                                    JsonObject newMsg = new JsonObject();
//                                    newMsg.put("topic",topic);
//                                    newMsg.put("payload",body.toString());
//                                    client.basicPublish("", nacosConfig.getProperty("tianyuan.rabbitmq.queue.cmd"), newMsg.toBuffer());
//                                    return;
//                                }
//                            }
//                        }
                    });
                    return Future.succeededFuture();
                });
            } else {
                log.error("RabbitMQToMqttVerticle Failed to connect to RabbitMQ broker");
            }
        });
    }
}
