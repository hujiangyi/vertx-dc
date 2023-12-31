package com.tdj.datacenter.handler;

import com.tdj.common.dingding.DingDingApiNew;
import com.tdj.datacenter.domain.StockConfig;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class CheckHandler {
    private Map<String,StockConfig> stockConfigMap = new HashMap<>();
    private DingDingApiNew dingDingApiNew = new DingDingApiNew();

    public Future<List<StockConfig>> fetchStock(Vertx vertx, Properties config) {
        Promise<List<StockConfig>> promise = Promise.promise();
        WebClientOptions options = new WebClientOptions()
                .setDefaultHost("hq.sinajs.cn")
                .setDefaultPort(443)
                .setSsl(true)
                .setTrustAll(true); // 在生产环境中，应该使用合适的SSL配置，而不是信任所有证书

        WebClient client = WebClient.create(vertx, options);

        String nums = config.getProperty("list");
        StringBuilder sb = new StringBuilder();
        sb.append("/list=").append(nums);
        String url = sb.toString();

        client.get(url)
                .putHeader("Accept", "*/*")
                .putHeader("Accept-Language", "zh-CN,zh;q=0.9")
                .putHeader("Connection", "keep-alive")
                .putHeader("DNT", "1")
                .putHeader("Referer", "https://finance.sina.com.cn/realstock/company/sz002010/nc.shtml")
                .putHeader("Sec-Fetch-Dest", "script")
                .putHeader("Sec-Fetch-Mode", "no-cors")
                .putHeader("Sec-Fetch-Site", "cross-site")
                .putHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .putHeader("sec-ch-ua", "\"Not_A Brand\";v=\"8\", \"Chromium\";v=\"120\", \"Google Chrome\";v=\"120\"")
                .putHeader("sec-ch-ua-mobile", "?0")
                .putHeader("sec-ch-ua-platform", "\"macOS\"")
                .putHeader("Cache-Control", "no-cache")
                .send(response -> {
                    if (response.succeeded()) {
                        try {
                            List<StockConfig> list = new ArrayList<>();
                            String body = response.result().bodyAsString();
                            String[] lines = body.split("\n");
                            for (String line : lines) {
                                String[] datas = line.split("=");
                                String num = datas[0].replace("var hq_str_", "");
                                String[] values = datas[1].substring(1, datas[1].length() - 3).split(",");
                                StockConfig stockConfig;
                                if (stockConfigMap.containsKey(num)) {
                                    stockConfig = stockConfigMap.get(num);
                                } else {
                                    stockConfig = makeStockConfig(num,config);
                                    stockConfigMap.put(num,stockConfig);
                                }
                                double now = Double.parseDouble(values[3]);
                                double high = Double.parseDouble(values[4]);
                                double low = Double.parseDouble(values[5]);
                                stockConfig.setNow(now);
                                stockConfig.setLasthigh(high);
                                stockConfig.setLastlow(low);
                                list.add(stockConfig);
                                log.trace("代码:{};今开;{};昨收:{};当前:{};最高:{};最低:{};\n" +
                                                "卖5:{}_{}\n" +
                                                "卖4:{}_{};\n" +
                                                "卖3:{}_{};\n" +
                                                "卖2:{}_{};\n" +
                                                "卖1:{}_{};\n" +
                                                "买1:{}_{};\n" +
                                                "买2:{}_{};\n" +
                                                "买3:{}_{};\n" +
                                                "买4:{}_{};\n" +
                                                "买5:{}_{};\n",
                                        num, values[1], values[2], values[3], values[4], values[5],
                                        Integer.parseInt(values[28]) / 100, values[29],//卖5
                                        Integer.parseInt(values[26]) / 100, values[27],//卖4
                                        Integer.parseInt(values[24]) / 100, values[25],//卖3
                                        Integer.parseInt(values[22]) / 100, values[23],//卖2
                                        Integer.parseInt(values[20]) / 100, values[21],//卖1
                                        Integer.parseInt(values[10]) / 100, values[11],//买1
                                        Integer.parseInt(values[12]) / 100, values[13],//买2
                                        Integer.parseInt(values[14]) / 100, values[15],//买3
                                        Integer.parseInt(values[16]) / 100, values[17],//买4
                                        Integer.parseInt(values[18]) / 100, values[19]//买5
                                );
                            }
                            promise.complete(list);
                        } catch (Exception e) {
                            log.error("", e);
                            promise.fail(e);
                        }
                    }
                    if (response.failed()) {
                        log.info("本次调用接口失败");
                        promise.fail(response.cause());
                    }
                });
        return promise.future();
    }

    public void doCheck(Vertx vertx, Properties config) {
        Future<List<StockConfig>> future = fetchStock(vertx,config);
        future.onComplete(handler->{
            List<StockConfig> list = handler.result();
            for (StockConfig sc : list) {
                if (sc.getLow() > sc.getLastlow()) {
                    sc.setLow(sc.getLastlow());
                    List<String> userIds = new ArrayList<String>();
                    userIds.add("manager2769");
                    JsonObject json = new JsonObject();
                    json.put("title",sc.getName() + "当前价格低于阈值告警");
                    json.put("text",sc.getName() + sc.getNote()+"，今天最低价格" + sc.getLastlow());
                    json.put("singleTitle","查询价格");
                    json.put("singleURL","http://hw.zhengyakeji.com:10360/api/fetchstock");
                    dingDingApiNew.batchSendTo(vertx,userIds,"sampleActionCard", json.toString());
                } else if (sc.getHigh() < sc.getLasthigh()) {
                    sc.setHigh(sc.getLasthigh());
                    List<String> userIds = new ArrayList<String>();
                    userIds.add("manager2769");
                    JsonObject json = new JsonObject();
                    json.put("title",sc.getName() + "当前价格高于阈值告警");
                    json.put("text",sc.getName() + sc.getNote()+"，今天最高价格" + sc.getLasthigh());
                    json.put("singleTitle","查询价格");
                    json.put("singleURL","http://hw.zhengyakeji.com:10360/api/fetchstock");
                    dingDingApiNew.batchSendTo(vertx,userIds,"sampleActionCard", json.toString());
                } else {
                    log.debug("代码:{};不符合要求{}跳过,当前最高{},当前最低{}",sc.getNum(),sc.getNote(),sc.getLasthigh(),sc.getLastlow());
                }
            }
        });
    }

    private StockConfig makeStockConfig(String num, Properties config) {
        String name = config.getProperty(num.concat("_n"));
        double low = Double.parseDouble(config.getProperty(num.concat("_l")));
        double high = Double.parseDouble(config.getProperty(num.concat("_h")));
        String note = "设置的目标为高于" + high + "低于" + low;
        StockConfig stockConfig = new StockConfig();
        stockConfig.setName(name);
        stockConfig.setNum(num);
        stockConfig.setLow(low);
        stockConfig.setHigh(high);
        stockConfig.setNote(note);
        return stockConfig;
    }
}
