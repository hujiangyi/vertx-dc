package com.tdj.common.mysql;

import com.tdj.common.Contact;
import com.tdj.common.ModuleInit;
import com.tdj.common.annotation.Utils;
import com.tdj.common.annotation.mysql.Column;
import com.tdj.common.annotation.mysql.Dao;
import com.tdj.common.utils.RedisUtils;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.templates.RowMapper;
import io.vertx.sqlclient.templates.SqlTemplate;
import io.vertx.sqlclient.templates.TupleMapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public abstract class BaseDao implements ModuleInit {
    protected RedisUtils redisUtils;
    protected JDBCPool pool;

    public void doTest() {
        String sql = "select id,receivable_amount,discount_amount,`time` from cw_sum where id = ?";
        List<Object> conditions = new ArrayList<>();
        conditions.add(16L);
        pool.preparedQuery(sql)
                // the emp id to look up
                .execute(Tuple.tuple(conditions))
                .onFailure(e -> {
                    log.error("",e);
                })
                .onSuccess(rows -> {
                    for (Row row : rows) {
                        System.out.println(row.getString("FIRST_NAME"));
                    }
                });
    }

    public Future<Boolean> init(Vertx vertx, Properties nacosConfig, JsonObject config){
        log.info("mysql init.");
        Promise<Boolean> promise = Promise.promise();
        pool = DBManager.getPool(vertx,nacosConfig);
        try {
            List<String> injectNames = new ArrayList<>();
            injectNames.add("redisUtils");
            Object obj = Contact.beanMap.get(Dao.class.getName()).get(this.getClass());
            for (String name:injectNames) {
                Field field = this.getClass().getSuperclass().getDeclaredField(name);
                field.setAccessible(true);
                field.set(obj,Contact.beanMap.get(Utils.class.getName()).get(RedisUtils.class));
            }
            log.info("mysql init success!");
            promise.complete();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("mysql init faild!",e);
            promise.fail(e);
        }
        return promise.future();
    }

    public <T extends Mapper<T>> Future<T> selectOne(String sql,Class<T> clazz) {
        try {
            T t = clazz.newInstance();
            return select(sql,t, t.paramMapper(), t.resultMappper()).compose(list ->{
                if (list.isEmpty()) {
                    return Future.succeededFuture(null);
                } else {
                    return Future.succeededFuture(list.get(0));
                }
            });
        } catch (InstantiationException | IllegalAccessException e) {
            return Future.failedFuture(e);
        }
    }
    public <T extends Mapper<T>>  Future<T> selectOne(String sql,T t) {
        return select(sql,t,t.paramMapper(),t.resultMappper()).compose(list->{
            if (list.isEmpty()) {
                return Future.succeededFuture(null);
            } else {
                return Future.succeededFuture(list.get(0));
            }
        });
    }

    public <T extends Mapper<T>>  Future<List<T>> select(String sql,Class<T> clazz) {
        try {
            T t = clazz.newInstance();
            return select(sql, t, t.paramMapper(), t.resultMappper());
        } catch (InstantiationException | IllegalAccessException e) {
            return Future.failedFuture(e);
        }
    }

    public <T extends Mapper<T>>  Future<List<T>> select(String sql,T t) {
        return select(sql, t, t.paramMapper(), t.resultMappper());
    }

    public <T> Future<List<T>> select(String sql, T t, Function<T, Map<String, Object>> paramMapper, RowMapper<T> resultMappper) {
        try {
            Future<RowSet<T>> rowSetFuture = SqlTemplate.forQuery(pool,sql).mapTo(resultMappper)
                    .mapFrom(TupleMapper.mapper(paramMapper)).execute(t);
            return fromRowSet(rowSetFuture);
        } catch (SecurityException e) {
            return Future.failedFuture(e);
        }
    }

    private <T> Future<List<T>> fromRowSet(Future<RowSet<T>> rowSetFuture) {
        List<T> list = new ArrayList<>();
        return rowSetFuture.compose(rows -> {
            for (T t : rows) {
                list.add(t);
            }
            return Future.succeededFuture(list);
        });
    }
}
