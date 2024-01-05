package com.tdj.datacenter.dao;

import com.tdj.common.annotation.mysql.Dao;
import com.tdj.common.BaseDao;
import com.tdj.datacenter.dao.pojo.Test;
import io.vertx.core.Future;

import java.util.List;

@Dao
public class MyTestDao extends BaseDao {
    public Future<List<Test>> myFirstSelect(String sql, Test test){
        return select(sql,test);
    }
}
