package com.tdj.datacenter.domain;

import lombok.Data;

@Data
public class StockConfig {
    private String name;
    private String num;
    private String note;
    private double low;
    private double high;
    private double lastlow;
    private double lasthigh;
    private double now;
}
