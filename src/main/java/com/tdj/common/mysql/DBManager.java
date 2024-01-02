package com.tdj.common.mysql;

import io.vertx.core.Vertx;
import io.vertx.jdbcclient.JDBCConnectOptions;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.PoolOptions;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public class DBManager {
    private static JDBCPool pool;
    public static JDBCPool getPool(Vertx vertx, Properties config) {
        if (pool == null) {
            pool = init(vertx,config);
        }
        return pool;
    }

    private static JDBCPool init(Vertx vertx, Properties config) {
        log.info("mysql pool init.");
        return JDBCPool.pool(
                vertx,
                new JDBCConnectOptions()
                        .setJdbcUrl(config.getProperty("mysql.url"))
                        .setUser(config.getProperty("mysql.username"))
                        .setPassword(config.getProperty("mysql.password")),
                new PoolOptions()
                        .setMaxSize(Integer.parseInt(config.getProperty("mysql.max_pool_size")))
                        .setName(config.getProperty("mysql.pool-name"))
        );
    }
}
