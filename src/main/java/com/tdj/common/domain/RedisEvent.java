package com.tdj.common.domain;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class RedisEvent<T> implements Serializable {
    private String event;
    private T key;
}
