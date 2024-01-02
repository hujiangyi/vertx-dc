package com.tdj.datacenter.dao.pojo;

import com.tdj.common.annotation.mysql.Column;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Test {
    @Column(name="id",primary = true)
    private Long id;
    @Column(name="receivable_amount")
    private BigDecimal receivableAmount;
    @Column(name="discount_amount")
    private BigDecimal discountAmount;
    @Column(name="time")
    private LocalDateTime time;
}
