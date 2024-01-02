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
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
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

    public Future<Boolean> init(Vertx vertx, Properties config){
        log.info("mysql init.");
        Promise<Boolean> promise = Promise.promise();
        pool = DBManager.getPool(vertx,config);
        try {
            List<String> injectNames = new ArrayList<>();
            injectNames.add("redisUtils");
            for (Class clazz : Contact.beanMap.get(Dao.class.getName()).keySet()) {
                Object obj = Contact.beanMap.get(Dao.class.getName()).get(clazz);
                for (String name:injectNames) {
                    Field field = clazz.getSuperclass().getDeclaredField(name);
                    field.setAccessible(true);
                    field.set(obj,Contact.beanMap.get(Utils.class.getName()).get(RedisUtils.class));
                }
            }
            log.info("mysql init success!");
            promise.complete();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("mysql init faild!",e);
            promise.fail(e);
        }
        return promise.future();
    }

    public <T> Future<List<T>> select(String sql,Class<T> clazz) {
        return select(sql,clazz,null);
    }

    public <T> Future<List<T>> select(String sql,T t) {
        JsonObject params = new JsonObject();
        Field[] fields = t.getClass().getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                Object obj = field.get(t);
                if (obj != null) {
                    params.put(field.getName(), obj);
                }
            } catch (Exception e) {
                log.trace("",e);
            }
        }
        return select(sql,(Class<T>) t.getClass(),params);
    }

    public <T> Future<List<T>> select(String sql, Class<T> clazz, JsonObject params) {
        List<String> args = extractArgs(sql);
        List<Object> conditions = new ArrayList<>();
        try {
            for (String arg : args) {
                if (params != null && params.containsKey(arg)) {
                    conditions.add(params.getValue(arg));
                }
            }
            sql = sql.replaceAll("#\\{.*\\}", "'?'");
            sql = sql.replaceAll("\\$\\{.*\\}", "?");
            Future<RowSet<Row>> rowSetFuture = pool.preparedQuery(sql).execute(Tuple.tuple(conditions));
            return fromRowSet(rowSetFuture,clazz);
        } catch (SecurityException e) {
            return Future.failedFuture(e);
        }
    }

    public <T> T fromRow(Row row,Class<T> clazz) throws InstantiationException, IllegalAccessException {
        try {
            T obj = clazz.newInstance();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                ;
                Annotation annotation = field.getAnnotation(Column.class);
                if (annotation != null) {
                    Column column = (Column) annotation;
                    try {
                        field.setAccessible(true);
                        field.set(obj, row.get(field.getType(), column.name()));
                    } catch (IllegalAccessException e) {
                        log.error("", e);
                    }
                }
            }
            return obj;
        } catch (InstantiationException | IllegalAccessException e) {
            throw e;
        }
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
        regex = "\\$\\{([^}]+)}";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(sql);
        while (matcher.find()) {
            // 提取括号内的内容（xxx）
            String arg = matcher.group(1);
            args.add(arg);
        }
        return args;
    }

    private <T> Future<List<T>> fromRowSet(Future<RowSet<Row>> rowSetFuture,Class<T> clazz) {
        List<T> list = new ArrayList<>();
        return rowSetFuture.compose(rows -> {
            for (Row row : rows) {
                try {
                    T t = BaseDao.this.fromRow(row, clazz);
                    list.add(t);
                } catch (Exception e) {
                    return Future.failedFuture(e);
                }
            }
            return Future.succeededFuture(list);
        });
    }
}
