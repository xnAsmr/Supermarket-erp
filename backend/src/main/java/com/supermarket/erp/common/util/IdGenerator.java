package com.supermarket.erp.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class IdGenerator {

    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private IdGenerator() {
    }

    /**
     * 生成订单号：yyyyMMddHHmmss + 3位门店编号 + 4位序列号
     */
    public static String generateOrderNo(Long storeId) {
        String storeNo = String.format("%03d", storeId % 1000);
        String seq = String.format("%04d", SEQUENCE.getAndIncrement() % 10000);
        return LocalDateTime.now().format(FORMATTER) + storeNo + seq;
    }

    /**
     * 生成支付流水号：PAY + yyyyMMddHHmmss + 6位序列号
     */
    public static String generatePaymentNo() {
        String seq = String.format("%06d", SEQUENCE.getAndIncrement() % 1000000);
        return "PAY" + LocalDateTime.now().format(FORMATTER) + seq;
    }

    /**
     * 生成交易流水号：TXN + yyyyMMddHHmmss + 6位序列号
     */
    public static String generateTxnNo() {
        String seq = String.format("%06d", SEQUENCE.getAndIncrement() % 1000000);
        return "TXN" + LocalDateTime.now().format(FORMATTER) + seq;
    }

    /**
     * 生成入库单号：RK + yyyyMMddHHmmss + 6位序列号
     */
    public static String generatePurchaseInNo() {
        String seq = String.format("%06d", SEQUENCE.getAndIncrement() % 1000000);
        return "RK" + LocalDateTime.now().format(FORMATTER) + seq;
    }

    /**
     * 生成出库单号：CK + yyyyMMddHHmmss + 6位序列号
     */
    public static String generateStockOutNo() {
        String seq = String.format("%06d", SEQUENCE.getAndIncrement() % 1000000);
        return "CK" + LocalDateTime.now().format(FORMATTER) + seq;
    }

    /**
     * 生成盘点单号：PD + yyyyMMddHHmmss + 6位序列号
     */
    public static String generateCheckNo() {
        String seq = String.format("%06d", SEQUENCE.getAndIncrement() % 1000000);
        return "PD" + LocalDateTime.now().format(FORMATTER) + seq;
    }

    /**
     * 生成退款单号：TK + yyyyMMddHHmmss + 6位序列号
     */
    public static String generateRefundNo() {
        String seq = String.format("%06d", SEQUENCE.getAndIncrement() % 1000000);
        return "TK" + LocalDateTime.now().format(FORMATTER) + seq;
    }

    /**
     * 生成请求ID
     */
    public static String generateRequestId() {
        return "req_" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
