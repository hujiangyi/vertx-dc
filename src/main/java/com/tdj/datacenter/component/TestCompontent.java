package com.tdj.datacenter.component;

import com.tdj.common.BaseComponent;
import com.tdj.common.annotation.Component;
import com.tdj.common.utils.RedisUtils;
import com.tdj.datacenter.dao.MyTestDao;
import com.tdj.datacenter.dao.pojo.Test;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class TestCompontent extends BaseComponent {
    private RedisUtils redisUtils;
    private MyTestDao myTestDao;

    public void doTest(){
        redisUtils.get("abc").compose(str->{
            log.info("test component:{}",str);
            Test test = new Test();
            test.setId(16L);
            return myTestDao.myFirstSelect(test.getTestSql(),test);
        }).compose(list->{
            log.info("test component list:{}",list);
            return Future.succeededFuture();
        });
    }
}
