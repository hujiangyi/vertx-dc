package com.tdj.datacenter.dao.pojo;

import com.tdj.common.annotation.mysql.Column;
import com.tdj.common.mysql.Mapper;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.templates.RowMapper;
import io.vertx.sqlclient.templates.TupleMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Data
public class Test extends Mapper<Test> {
    private final String testSql = "select id,receivable_amount,discount_amount,`time` from cw_sum where id = #{id}";
    private Long id;
    private double receivableAmount;
    private double discountAmount;
    private LocalDateTime time;

    @Override
    public RowMapper<Test> resultMappper() {
        return row -> {
            Test test = new Test();
            test.id = row.getLong("id");
            test.receivableAmount = row.getDouble("receivable_amount");
            test.discountAmount = row.getDouble("discount_amount");
            test.time = row.getLocalDateTime("time");
            return test;
        };
    }

    @Override
    public Function<Test, Map<String, Object>> paramMapper() {
        return test -> {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("id", test.id);
            return parameters;
        };
    }
}
