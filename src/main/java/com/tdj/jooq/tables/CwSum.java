/*
 * This file is generated by jOOQ.
 */
package com.tdj.jooq.tables;


import com.tdj.jooq.Keys;
import com.tdj.jooq.Test;
import com.tdj.jooq.tables.records.CwSumRecord;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Identity;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row9;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;


/**
 * 车位统计
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CwSum extends TableImpl<CwSumRecord> {

    private static final long serialVersionUID = 1L;

    /**
     * The reference instance of <code>test.cw_sum</code>
     */
    public static final CwSum CW_SUM = new CwSum();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<CwSumRecord> getRecordType() {
        return CwSumRecord.class;
    }

    /**
     * The column <code>test.cw_sum.id</code>.
     */
    public final TableField<CwSumRecord, Long> ID = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "");

    /**
     * The column <code>test.cw_sum.receivable_amount</code>. 应收金额
     */
    public final TableField<CwSumRecord, BigDecimal> RECEIVABLE_AMOUNT = createField(DSL.name("receivable_amount"), SQLDataType.DECIMAL(10, 2).defaultValue(DSL.inline("0.00", SQLDataType.DECIMAL)), this, "应收金额");

    /**
     * The column <code>test.cw_sum.discount_amount</code>. 抵扣金额
     */
    public final TableField<CwSumRecord, BigDecimal> DISCOUNT_AMOUNT = createField(DSL.name("discount_amount"), SQLDataType.DECIMAL(10, 2).defaultValue(DSL.inline("0.00", SQLDataType.DECIMAL)), this, "抵扣金额");

    /**
     * The column <code>test.cw_sum.parking_coupon_discount_amount</code>. 商家停车券抵扣金额
     */
    public final TableField<CwSumRecord, BigDecimal> PARKING_COUPON_DISCOUNT_AMOUNT = createField(DSL.name("parking_coupon_discount_amount"), SQLDataType.DECIMAL(10, 2).defaultValue(DSL.inline("0.00", SQLDataType.DECIMAL)), this, "商家停车券抵扣金额");

    /**
     * The column <code>test.cw_sum.remission_amount</code>. 车位减免金额(元)
     */
    public final TableField<CwSumRecord, BigDecimal> REMISSION_AMOUNT = createField(DSL.name("remission_amount"), SQLDataType.DECIMAL(10, 2).defaultValue(DSL.inline("0.00", SQLDataType.DECIMAL)), this, "车位减免金额(元)");

    /**
     * The column <code>test.cw_sum.actual_amount</code>. 实收金额
     */
    public final TableField<CwSumRecord, BigDecimal> ACTUAL_AMOUNT = createField(DSL.name("actual_amount"), SQLDataType.DECIMAL(10, 2).nullable(false).defaultValue(DSL.inline("0.00", SQLDataType.DECIMAL)), this, "实收金额");

    /**
     * The column <code>test.cw_sum.cancel_amount</code>. 注销金额
     */
    public final TableField<CwSumRecord, BigDecimal> CANCEL_AMOUNT = createField(DSL.name("cancel_amount"), SQLDataType.DECIMAL(10, 2).defaultValue(DSL.inline("0.00", SQLDataType.DECIMAL)), this, "注销金额");

    /**
     * The column <code>test.cw_sum.park_name</code>. 车场名称
     */
    public final TableField<CwSumRecord, String> PARK_NAME = createField(DSL.name("park_name"), SQLDataType.VARCHAR(100).nullable(false), this, "车场名称");

    /**
     * The column <code>test.cw_sum.time</code>.
     */
    public final TableField<CwSumRecord, LocalDateTime> TIME = createField(DSL.name("time"), SQLDataType.LOCALDATETIME(0).nullable(false).defaultValue(DSL.field("CURRENT_TIMESTAMP", SQLDataType.LOCALDATETIME)), this, "");

    private CwSum(Name alias, Table<CwSumRecord> aliased) {
        this(alias, aliased, null);
    }

    private CwSum(Name alias, Table<CwSumRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment("车位统计"), TableOptions.table());
    }

    /**
     * Create an aliased <code>test.cw_sum</code> table reference
     */
    public CwSum(String alias) {
        this(DSL.name(alias), CW_SUM);
    }

    /**
     * Create an aliased <code>test.cw_sum</code> table reference
     */
    public CwSum(Name alias) {
        this(alias, CW_SUM);
    }

    /**
     * Create a <code>test.cw_sum</code> table reference
     */
    public CwSum() {
        this(DSL.name("cw_sum"), null);
    }

    public <O extends Record> CwSum(Table<O> child, ForeignKey<O, CwSumRecord> key) {
        super(child, key, CW_SUM);
    }

    @Override
    public Schema getSchema() {
        return Test.TEST;
    }

    @Override
    public Identity<CwSumRecord, Long> getIdentity() {
        return (Identity<CwSumRecord, Long>) super.getIdentity();
    }

    @Override
    public UniqueKey<CwSumRecord> getPrimaryKey() {
        return Keys.KEY_CW_SUM_PRIMARY;
    }

    @Override
    public List<UniqueKey<CwSumRecord>> getKeys() {
        return Arrays.<UniqueKey<CwSumRecord>>asList(Keys.KEY_CW_SUM_PRIMARY);
    }

    @Override
    public CwSum as(String alias) {
        return new CwSum(DSL.name(alias), this);
    }

    @Override
    public CwSum as(Name alias) {
        return new CwSum(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public CwSum rename(String name) {
        return new CwSum(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public CwSum rename(Name name) {
        return new CwSum(name, null);
    }

    // -------------------------------------------------------------------------
    // Row9 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row9<Long, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, BigDecimal, String, LocalDateTime> fieldsRow() {
        return (Row9) super.fieldsRow();
    }
}
