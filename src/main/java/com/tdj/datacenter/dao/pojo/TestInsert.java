package com.tdj.datacenter.dao.pojo;

import com.tdj.common.mysql.Mapper;
import io.vertx.sqlclient.templates.RowMapper;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Data
public class TestInsert extends Mapper<TestInsert> {
    private final String testSql = "select id,receivable_amount,discount_amount,`time` from cw_sum where id = #{id}";
    private Long id;
    private double receivableAmount;
    private double discountAmount;
    private LocalDateTime time;

    @Override
    public RowMapper<TestInsert> resultMappper() {
        return null;
    }

    @Override
    public Function<TestInsert, Map<String, Object>> paramMapper() {
        return test -> {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("id", test.id);
            parameters.put("receivable_amount", test.receivableAmount);
            parameters.put("discount_amount", test.discountAmount);
            parameters.put("time", LocalDateTime.now());
            return parameters;
        };
    }
}
