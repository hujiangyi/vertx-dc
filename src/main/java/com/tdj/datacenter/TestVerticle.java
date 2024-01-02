package com.tdj.datacenter;

import com.tdj.common.BaseVerticle;
import com.tdj.datacenter.handler.CheckHandler;
import io.vertx.core.eventbus.EventBus;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TestVerticle extends BaseVerticle {

    @Override
    public void doInit() {
        String sql = "select id,receivable_amount,discount_amount,time from cw_sum where id = #{id} and time between #{startTime} and #{endTime}";
        List<String> args = extractArgs(sql);
        log.info(args.toString());
    }
    private List<String> extractArgs(String sql) {
        List<String> args = new ArrayList<>();
        String regex = "#\\{([^}]+)}";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            // 提取括号内的内容（xxx）
            String arg = matcher.group(1);
            args.add(arg);
        }
        return args;
    }
}