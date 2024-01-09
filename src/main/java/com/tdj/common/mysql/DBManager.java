package com.tdj.common.mysql;

import com.alibaba.druid.pool.DruidDataSource;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.spi.DataSourceProvider;
import io.vertx.jdbcclient.JDBCPool;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Configuration;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class DBManager {
    private static JsonObject dataSourceConfig = new JsonObject();
    private static DataSource dataSource;
    private static JDBCPool pool;
    public static JDBCPool getPool(Vertx vertx, Properties config) throws SQLException {
        if (pool == null) {
            pool = init(vertx,config);
        }
        return pool;
    }

    private static JDBCPool init(Vertx vertx, Properties config) throws SQLException {
        log.info("mysql pool init.");
        dataSourceConfig.put("provider_class",config.getProperty("datasource.provider_class"));
        dataSourceConfig.put("driverClassName",config.getProperty("druid.driverClassName"));
        dataSourceConfig.put("url",config.getProperty("druid.url"));
        dataSourceConfig.put("username",config.getProperty("druid.username"));
        dataSourceConfig.put("password",config.getProperty("druid.password"));
        dataSourceConfig.put("maxActive",Integer.parseInt(config.getProperty("druid.max-active")));
        dataSourceConfig.put("initialSize",Integer.parseInt(config.getProperty("druid.initial-size")));
        dataSourceConfig.put("minIdle",Integer.parseInt(config.getProperty("druid.min-idle")));
        dataSourceConfig.put("testWhileIdle",Boolean.parseBoolean(config.getProperty("druid.test-while-idle")));
        dataSourceConfig.put("testOnBorrow",Boolean.parseBoolean(config.getProperty("druid.test-on-borrow")));
        dataSourceConfig.put("validationQuery",config.getProperty("druid.validation-query"));
        dataSourceConfig.put("removeAbandoned",Boolean.parseBoolean(config.getProperty("druid.remove-abandoned")));
        dataSource = createDefaultDataSource(dataSourceConfig);
        return JDBCPool.pool(vertx, DataSourceProvider.create(dataSourceConfig));
    }

    public static Configuration getJooqConfiguration() throws SQLException {
         return new DefaultConfiguration().set(SQLDialect.MYSQL).set(new DataSourceConnectionProvider(createDefaultDataSource(dataSourceConfig)));
    }

    private static DataSource createDefaultDataSource(JsonObject dataSourceConfig) throws SQLException {
        if (dataSource == null) {
            dataSource = getDataSource(dataSourceConfig);
        }
        return dataSource;
    }

    private static DataSource getDataSource(JsonObject config) throws SQLException {
        DruidDataSource ds = new DruidDataSource();
        Method[] methods = DruidDataSource.class.getMethods();
        Map<String,Method> methodmap = new HashMap<>();
        for (Method method : methods) {
            methodmap.put(method.getName(),method);
        }

        for (Map.Entry<String, Object> entry : config) {
            String name = entry.getKey();

            if ("provider_class".equals(name)) {
                continue;
            }

            String mName = "set" + name.substring(0, 1).toUpperCase() + name.substring(1);

            try {
                Class paramClazz = entry.getValue().getClass();
                if(paramClazz.equals(Integer.class)){
                    paramClazz = int.class;
                }else if(paramClazz.equals(Long.class)){
                    paramClazz = long.class;
                }else if(paramClazz.equals(Boolean.class)){
                    paramClazz = boolean.class;
                }
                Method method = DruidDataSource.class.getMethod(mName, paramClazz);
                method.invoke(ds, entry.getValue());
            } catch (NoSuchMethodException e) {
                log.warn("no such method:" + mName);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return ds;
    }
}
