/*
 * This file is generated by jOOQ.
 */
package com.tdj.jooq.tables.records;


import com.tdj.jooq.tables.CwSum;
import com.tdj.jooq.tables.interfaces.ICwSum;

import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record9;
import org.jooq.Row9;
import org.jooq.impl.UpdatableRecordImpl;


import static io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo.*;
/**
 * 车位统计
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CwSumRecord extends UpdatableRecordImpl<CwSumRecord> implements VertxPojo, Record9<Long, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, String, LocalDateTime>, ICwSum {

    private static final long serialVersionUID = 1L;

    /**
     * Setter for <code>test.cw_sum.id</code>.
     */
    @Override
    public CwSumRecord setId(Long value) {
        set(0, value);
        return this;
    }

    /**
     * Getter for <code>test.cw_sum.id</code>.
     */
    @Override
    public Long getId() {
        return (Long) get(0);
    }

    /**
     * Setter for <code>test.cw_sum.receivable_amount</code>. 应收金额
     */
    @Override
    public CwSumRecord setReceivableAmount(BigDecimal value) {
        set(1, value);
        return this;
    }

    /**
     * Getter for <code>test.cw_sum.receivable_amount</code>. 应收金额
     */
    @Override
    public BigDecimal getReceivableAmount() {
        return (BigDecimal) get(1);
    }

    /**
     * Setter for <code>test.cw_sum.discount_amount</code>. 抵扣金额
     */
    @Override
    public CwSumRecord setDiscountAmount(BigDecimal value) {
        set(2, value);
        return this;
    }

    /**
     * Getter for <code>test.cw_sum.discount_amount</code>. 抵扣金额
     */
    @Override
    public BigDecimal getDiscountAmount() {
        return (BigDecimal) get(2);
    }

    /**
     * Setter for <code>test.cw_sum.parking_coupon_discount_amount</code>. 商家停车券抵扣金额
     */
    @Override
    public CwSumRecord setParkingCouponDiscountAmount(BigDecimal value) {
        set(3, value);
        return this;
    }

    /**
     * Getter for <code>test.cw_sum.parking_coupon_discount_amount</code>. 商家停车券抵扣金额
     */
    @Override
    public BigDecimal getParkingCouponDiscountAmount() {
        return (BigDecimal) get(3);
    }

    /**
     * Setter for <code>test.cw_sum.remission_amount</code>. 车位减免金额(元)
     */
    @Override
    public CwSumRecord setRemissionAmount(BigDecimal value) {
        set(4, value);
        return this;
    }

    /**
     * Getter for <code>test.cw_sum.remission_amount</code>. 车位减免金额(元)
     */
    @Override
    public BigDecimal getRemissionAmount() {
        return (BigDecimal) get(4);
    }

    /**
     * Setter for <code>test.cw_sum.actual_amount</code>. 实收金额
     */
    @Override
    public CwSumRecord setActualAmount(BigDecimal value) {
        set(5, value);
        return this;
    }

    /**
     * Getter for <code>test.cw_sum.actual_amount</code>. 实收金额
     */
    @Override
    public BigDecimal getActualAmount() {
        return (BigDecimal) get(5);
    }

    /**
     * Setter for <code>test.cw_sum.cancel_amount</code>. 注销金额
     */
    @Override
    public CwSumRecord setCancelAmount(BigDecimal value) {
        set(6, value);
        return this;
    }

    /**
     * Getter for <code>test.cw_sum.cancel_amount</code>. 注销金额
     */
    @Override
    public BigDecimal getCancelAmount() {
        return (BigDecimal) get(6);
    }

    /**
     * Setter for <code>test.cw_sum.park_name</code>. 车场名称
     */
    @Override
    public CwSumRecord setParkName(String value) {
        set(7, value);
        return this;
    }

    /**
     * Getter for <code>test.cw_sum.park_name</code>. 车场名称
     */
    @Override
    public String getParkName() {
        return (String) get(7);
    }

    /**
     * Setter for <code>test.cw_sum.time</code>.
     */
    @Override
    public CwSumRecord setTime(LocalDateTime value) {
        set(8, value);
        return this;
    }

    /**
     * Getter for <code>test.cw_sum.time</code>.
     */
    @Override
    public LocalDateTime getTime() {
        return (LocalDateTime) get(8);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    @Override
    public Record1<Long> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record9 type implementation
    // -------------------------------------------------------------------------

    @Override
    public Row9<Long, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, String, LocalDateTime> fieldsRow() {
        return (Row9) super.fieldsRow();
    }

    @Override
    public Row9<Long, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, String, LocalDateTime> valuesRow() {
        return (Row9) super.valuesRow();
    }

    @Override
    public Field<Long> field1() {
        return CwSum.CW_SUM.ID;
    }

    @Override
    public Field<BigDecimal> field2() {
        return CwSum.CW_SUM.RECEIVABLE_AMOUNT;
    }

    @Override
    public Field<BigDecimal> field3() {
        return CwSum.CW_SUM.DISCOUNT_AMOUNT;
    }

    @Override
    public Field<BigDecimal> field4() {
        return CwSum.CW_SUM.PARKING_COUPON_DISCOUNT_AMOUNT;
    }

    @Override
    public Field<BigDecimal> field5() {
        return CwSum.CW_SUM.REMISSION_AMOUNT;
    }

    @Override
    public Field<BigDecimal> field6() {
        return CwSum.CW_SUM.ACTUAL_AMOUNT;
    }

    @Override
    public Field<BigDecimal> field7() {
        return CwSum.CW_SUM.CANCEL_AMOUNT;
    }

    @Override
    public Field<String> field8() {
        return CwSum.CW_SUM.PARK_NAME;
    }

    @Override
    public Field<LocalDateTime> field9() {
        return CwSum.CW_SUM.TIME;
    }

    @Override
    public Long component1() {
        return getId();
    }

    @Override
    public BigDecimal component2() {
        return getReceivableAmount();
    }

    @Override
    public BigDecimal component3() {
        return getDiscountAmount();
    }

    @Override
    public BigDecimal component4() {
        return getParkingCouponDiscountAmount();
    }

    @Override
    public BigDecimal component5() {
        return getRemissionAmount();
    }

    @Override
    public BigDecimal component6() {
        return getActualAmount();
    }

    @Override
    public BigDecimal component7() {
        return getCancelAmount();
    }

    @Override
    public String component8() {
        return getParkName();
    }

    @Override
    public LocalDateTime component9() {
        return getTime();
    }

    @Override
    public Long value1() {
        return getId();
    }

    @Override
    public BigDecimal value2() {
        return getReceivableAmount();
    }

    @Override
    public BigDecimal value3() {
        return getDiscountAmount();
    }

    @Override
    public BigDecimal value4() {
        return getParkingCouponDiscountAmount();
    }

    @Override
    public BigDecimal value5() {
        return getRemissionAmount();
    }

    @Override
    public BigDecimal value6() {
        return getActualAmount();
    }

    @Override
    public BigDecimal value7() {
        return getCancelAmount();
    }

    @Override
    public String value8() {
        return getParkName();
    }

    @Override
    public LocalDateTime value9() {
        return getTime();
    }

    @Override
    public CwSumRecord value1(Long value) {
        setId(value);
        return this;
    }

    @Override
    public CwSumRecord value2(BigDecimal value) {
        setReceivableAmount(value);
        return this;
    }

    @Override
    public CwSumRecord value3(BigDecimal value) {
        setDiscountAmount(value);
        return this;
    }

    @Override
    public CwSumRecord value4(BigDecimal value) {
        setParkingCouponDiscountAmount(value);
        return this;
    }

    @Override
    public CwSumRecord value5(BigDecimal value) {
        setRemissionAmount(value);
        return this;
    }

    @Override
    public CwSumRecord value6(BigDecimal value) {
        setActualAmount(value);
        return this;
    }

    @Override
    public CwSumRecord value7(BigDecimal value) {
        setCancelAmount(value);
        return this;
    }

    @Override
    public CwSumRecord value8(String value) {
        setParkName(value);
        return this;
    }

    @Override
    public CwSumRecord value9(LocalDateTime value) {
        setTime(value);
        return this;
    }

    @Override
    public CwSumRecord values(Long value1, BigDecimal value2, BigDecimal value3, BigDecimal value4, BigDecimal value5, BigDecimal value6, BigDecimal value7, String value8, LocalDateTime value9) {
        value1(value1);
        value2(value2);
        value3(value3);
        value4(value4);
        value5(value5);
        value6(value6);
        value7(value7);
        value8(value8);
        value9(value9);
        return this;
    }

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    @Override
    public void from(ICwSum from) {
        setId(from.getId());
        setReceivableAmount(from.getReceivableAmount());
        setDiscountAmount(from.getDiscountAmount());
        setParkingCouponDiscountAmount(from.getParkingCouponDiscountAmount());
        setRemissionAmount(from.getRemissionAmount());
        setActualAmount(from.getActualAmount());
        setCancelAmount(from.getCancelAmount());
        setParkName(from.getParkName());
        setTime(from.getTime());
    }

    @Override
    public <E extends ICwSum> E into(E into) {
        into.from(this);
        return into;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached CwSumRecord
     */
    public CwSumRecord() {
        super(CwSum.CW_SUM);
    }

    /**
     * Create a detached, initialised CwSumRecord
     */
    public CwSumRecord(Long id, BigDecimal receivableAmount, BigDecimal discountAmount, BigDecimal parkingCouponDiscountAmount, BigDecimal remissionAmount, BigDecimal actualAmount, BigDecimal cancelAmount, String parkName, LocalDateTime time) {
        super(CwSum.CW_SUM);

        setId(id);
        setReceivableAmount(receivableAmount);
        setDiscountAmount(discountAmount);
        setParkingCouponDiscountAmount(parkingCouponDiscountAmount);
        setRemissionAmount(remissionAmount);
        setActualAmount(actualAmount);
        setCancelAmount(cancelAmount);
        setParkName(parkName);
        setTime(time);
    }

        public CwSumRecord(io.vertx.core.json.JsonObject json) {
                this();
                fromJson(json);
        }
}
