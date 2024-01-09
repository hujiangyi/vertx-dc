/*
 * This file is generated by jOOQ.
 */
package com.tdj.jooq.tables.daos;


import com.tdj.jooq.tables.CwSum;
import com.tdj.jooq.tables.records.CwSumRecord;

import io.github.jklingsporn.vertx.jooq.shared.internal.AbstractVertxDAO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;

import org.jooq.Configuration;


import java.util.List;
import io.vertx.core.Future;
import io.github.jklingsporn.vertx.jooq.classic.jdbc.JDBCClassicQueryExecutor;
/**
 * 车位统计
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class CwSumDao extends AbstractVertxDAO<CwSumRecord, com.tdj.jooq.tables.pojos.CwSum, Long, Future<List<com.tdj.jooq.tables.pojos.CwSum>>, Future<com.tdj.jooq.tables.pojos.CwSum>, Future<Integer>, Future<Long>> implements io.github.jklingsporn.vertx.jooq.classic.VertxDAO<CwSumRecord,com.tdj.jooq.tables.pojos.CwSum,Long> {

        /**
     * @param configuration The Configuration used for rendering and query execution.
     * @param vertx the vertx instance
     */
        public CwSumDao(Configuration configuration, io.vertx.core.Vertx vertx) {
                super(CwSum.CW_SUM, com.tdj.jooq.tables.pojos.CwSum.class, new JDBCClassicQueryExecutor<CwSumRecord,com.tdj.jooq.tables.pojos.CwSum,Long>(configuration,com.tdj.jooq.tables.pojos.CwSum.class,vertx));
        }

        @Override
        protected Long getId(com.tdj.jooq.tables.pojos.CwSum object) {
                return object.getId();
        }

        /**
     * Find records that have <code>receivable_amount IN (values)</code> asynchronously
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByReceivableAmount(Collection<BigDecimal> values) {
                return findManyByCondition(CwSum.CW_SUM.RECEIVABLE_AMOUNT.in(values));
        }

        /**
     * Find records that have <code>receivable_amount IN (values)</code> asynchronously limited by the given limit
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByReceivableAmount(Collection<BigDecimal> values, int limit) {
                return findManyByCondition(CwSum.CW_SUM.RECEIVABLE_AMOUNT.in(values),limit);
        }

        /**
     * Find records that have <code>discount_amount IN (values)</code> asynchronously
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByDiscountAmount(Collection<BigDecimal> values) {
                return findManyByCondition(CwSum.CW_SUM.DISCOUNT_AMOUNT.in(values));
        }

        /**
     * Find records that have <code>discount_amount IN (values)</code> asynchronously limited by the given limit
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByDiscountAmount(Collection<BigDecimal> values, int limit) {
                return findManyByCondition(CwSum.CW_SUM.DISCOUNT_AMOUNT.in(values),limit);
        }

        /**
     * Find records that have <code>parking_coupon_discount_amount IN (values)</code> asynchronously
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByParkingCouponDiscountAmount(Collection<BigDecimal> values) {
                return findManyByCondition(CwSum.CW_SUM.PARKING_COUPON_DISCOUNT_AMOUNT.in(values));
        }

        /**
     * Find records that have <code>parking_coupon_discount_amount IN (values)</code> asynchronously limited by the given limit
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByParkingCouponDiscountAmount(Collection<BigDecimal> values, int limit) {
                return findManyByCondition(CwSum.CW_SUM.PARKING_COUPON_DISCOUNT_AMOUNT.in(values),limit);
        }

        /**
     * Find records that have <code>remission_amount IN (values)</code> asynchronously
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByRemissionAmount(Collection<BigDecimal> values) {
                return findManyByCondition(CwSum.CW_SUM.REMISSION_AMOUNT.in(values));
        }

        /**
     * Find records that have <code>remission_amount IN (values)</code> asynchronously limited by the given limit
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByRemissionAmount(Collection<BigDecimal> values, int limit) {
                return findManyByCondition(CwSum.CW_SUM.REMISSION_AMOUNT.in(values),limit);
        }

        /**
     * Find records that have <code>actual_amount IN (values)</code> asynchronously
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByActualAmount(Collection<BigDecimal> values) {
                return findManyByCondition(CwSum.CW_SUM.ACTUAL_AMOUNT.in(values));
        }

        /**
     * Find records that have <code>actual_amount IN (values)</code> asynchronously limited by the given limit
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByActualAmount(Collection<BigDecimal> values, int limit) {
                return findManyByCondition(CwSum.CW_SUM.ACTUAL_AMOUNT.in(values),limit);
        }

        /**
     * Find records that have <code>cancel_amount IN (values)</code> asynchronously
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByCancelAmount(Collection<BigDecimal> values) {
                return findManyByCondition(CwSum.CW_SUM.CANCEL_AMOUNT.in(values));
        }

        /**
     * Find records that have <code>cancel_amount IN (values)</code> asynchronously limited by the given limit
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByCancelAmount(Collection<BigDecimal> values, int limit) {
                return findManyByCondition(CwSum.CW_SUM.CANCEL_AMOUNT.in(values),limit);
        }

        /**
     * Find records that have <code>park_name IN (values)</code> asynchronously
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByParkName(Collection<String> values) {
                return findManyByCondition(CwSum.CW_SUM.PARK_NAME.in(values));
        }

        /**
     * Find records that have <code>park_name IN (values)</code> asynchronously limited by the given limit
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByParkName(Collection<String> values, int limit) {
                return findManyByCondition(CwSum.CW_SUM.PARK_NAME.in(values),limit);
        }

        /**
     * Find records that have <code>time IN (values)</code> asynchronously
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByTime(Collection<LocalDateTime> values) {
                return findManyByCondition(CwSum.CW_SUM.TIME.in(values));
        }

        /**
     * Find records that have <code>time IN (values)</code> asynchronously limited by the given limit
     */
        public Future<List<com.tdj.jooq.tables.pojos.CwSum>> findManyByTime(Collection<LocalDateTime> values, int limit) {
                return findManyByCondition(CwSum.CW_SUM.TIME.in(values),limit);
        }

        @Override
        public JDBCClassicQueryExecutor<CwSumRecord,com.tdj.jooq.tables.pojos.CwSum,Long> queryExecutor(){
                return (JDBCClassicQueryExecutor<CwSumRecord,com.tdj.jooq.tables.pojos.CwSum,Long>) super.queryExecutor();
        }
}
