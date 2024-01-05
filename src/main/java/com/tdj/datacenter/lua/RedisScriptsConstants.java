package com.tdj.datacenter.lua;

/**
 * redis lua脚本
 *
 * @author zhoubin
 * @date 2022/7/6
 */
public interface RedisScriptsConstants {
        /**
         * 过期时间内的全局唯一ID
         */
        String SERIAL_NUMBER = "local serialNumber;\n" +
                "serialNumber = redis.call('incr', KEYS[1])\n" +
                "redis.call('pexpire', KEYS[1], ARGV[1])\n" +
                "return serialNumber";
    }

