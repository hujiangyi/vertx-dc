package com.tdj.datacenter.dao;

import com.tdj.common.annotation.mysql.Dao;
import com.tdj.common.BaseDao;
import com.tdj.common.mysql.DBManager;
import com.tdj.datacenter.dao.pojo.Test;
import com.tdj.jooq.tables.daos.CwSumDao;
import com.tdj.jooq.tables.pojos.CwSum;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

@Dao
public class MyTestDao extends BaseDao {
    private CwSumDao cwSumDao;

    @Override
    protected void initDao(Vertx vertx, Properties nacosConfig, JsonObject config) throws SQLException {
        super.initDao(vertx, nacosConfig, config);
        cwSumDao = new CwSumDao(DBManager.getJooqConfiguration(),vertx);
    }

    public Future<List<CwSum>> myFirstSelect(){
        return cwSumDao.findAll();
    }
    public Future<Integer> myInsert(CwSum cwSum) {
        return cwSumDao.insert(cwSum);
    }
}
