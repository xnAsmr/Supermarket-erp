package com.supermarket.erp.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.erp.module.order.entity.OrderPayment;
import com.supermarket.erp.module.order.vo.DailyPaymentFlowVO;
import com.supermarket.erp.module.order.vo.PaymentFlowVO;
import com.supermarket.erp.module.order.vo.PaymentMethodStatVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderPaymentMapper extends BaseMapper<OrderPayment> {

    @Select("<script>" +
            "SELECT " +
            "  pay_date, " +
            "  payment_method, " +
            "  payment_method_name, " +
            "  SUM(total_amount) AS total_amount, " +
            "  SUM(total_amount) AS received_amount, " +
            "  SUM(transaction_count) AS transaction_count " +
            "FROM (" +
            "  SELECT " +
            "    DATE(pay_time) AS pay_date, " +
            "    op.payment_method, " +
            "    COALESCE(pm.name, op.payment_method) AS payment_method_name, " +
            "    SUM(CASE WHEN op.amount &gt; 0 THEN op.amount ELSE 0 END) AS total_amount, " +
            "    COUNT(*) AS transaction_count " +
            "  FROM oms_order_payment op " +
            "  LEFT JOIN sys_payment_method pm ON pm.code = op.payment_method COLLATE utf8mb4_general_ci AND pm.tenant_id = op.tenant_id AND pm.deleted = 0 " +
            "  WHERE op.tenant_id = #{tenantId} AND op.deleted = 0 " +
            "    AND op.pay_time &gt;= #{startDate} AND op.pay_time &lt; #{endDate} " +
            "    AND op.payment_method != 'stored' " +
            "  <if test='storeId != null'>AND op.store_id = #{storeId}</if> " +
            "  <if test='paymentMethod != null and paymentMethod != \"\"'>AND op.payment_method = #{paymentMethod}</if> " +
            "  GROUP BY DATE(pay_time), op.payment_method, pm.name " +
            "  UNION ALL " +
            "  SELECT " +
            "    DATE(r.create_time) AS pay_date, " +
            "    r.pay_method COLLATE utf8mb4_general_ci AS payment_method, " +
            "    COALESCE(pm2.name, r.pay_method) COLLATE utf8mb4_general_ci AS payment_method_name, " +
            "    r.amount AS total_amount, " +
            "    1 AS transaction_count " +
            "  FROM ums_member_recharge_log r " +
            "  LEFT JOIN sys_payment_method pm2 ON pm2.code = r.pay_method COLLATE utf8mb4_general_ci AND pm2.tenant_id = r.tenant_id AND pm2.deleted = 0 " +
            "  WHERE r.tenant_id = #{tenantId} " +
            "    AND r.create_time &gt;= #{startDate} AND r.create_time &lt; #{endDate} " +
            "  <if test='storeId != null'>AND 1 = 0</if> " +
            "  <if test='paymentMethod != null and paymentMethod != \"\"'>AND r.pay_method = #{paymentMethod}</if> " +
            ") t " +
            "GROUP BY pay_date, payment_method, payment_method_name " +
            "ORDER BY pay_date DESC, total_amount DESC" +
            "</script>")
    List<PaymentFlowVO> selectPaymentFlow(
            @Param("tenantId") Long tenantId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate,
            @Param("storeId") Long storeId,
            @Param("paymentMethod") String paymentMethod
    );

    @Select("<script>" +
            "SELECT " +
            "  payment_method, " +
            "  payment_method_name, " +
            "  SUM(total_inflow) AS total_inflow, " +
            "  SUM(total_outflow) AS total_outflow, " +
            "  SUM(total_inflow) - SUM(total_outflow) AS balance, " +
            "  SUM(total_in_count) AS total_in_count, " +
            "  SUM(total_out_count) AS total_out_count " +
            "FROM (" +
            "  SELECT " +
            "    op.payment_method, " +
            "    COALESCE(pm.name, op.payment_method) AS payment_method_name, " +
            "    CASE WHEN op.payment_method = 'stored' THEN 0 ELSE SUM(CASE WHEN op.amount &gt; 0 THEN op.amount ELSE 0 END) END AS total_inflow, " +
            "    CASE WHEN op.payment_method = 'stored' THEN SUM(op.amount) ELSE SUM(CASE WHEN op.amount &lt; 0 THEN ABS(op.amount) ELSE 0 END) END AS total_outflow, " +
            "    CASE WHEN op.payment_method = 'stored' THEN 0 ELSE SUM(CASE WHEN op.amount &gt; 0 THEN 1 ELSE 0 END) END AS total_in_count, " +
            "    CASE WHEN op.payment_method = 'stored' THEN COUNT(*) ELSE SUM(CASE WHEN op.amount &lt; 0 THEN 1 ELSE 0 END) END AS total_out_count " +
            "  FROM oms_order_payment op " +
            "  LEFT JOIN sys_payment_method pm ON pm.code = op.payment_method COLLATE utf8mb4_general_ci AND pm.tenant_id = op.tenant_id AND pm.deleted = 0 " +
            "  WHERE op.tenant_id = #{tenantId} AND op.deleted = 0 " +
            "  GROUP BY op.payment_method, pm.name " +
            "  UNION ALL " +
            "  SELECT " +
            "    r.pay_method COLLATE utf8mb4_general_ci AS payment_method, " +
            "    COALESCE(pm2.name, r.pay_method) COLLATE utf8mb4_general_ci AS payment_method_name, " +
            "    r.amount AS total_inflow, " +
            "    0 AS total_outflow, " +
            "    1 AS total_in_count, " +
            "    0 AS total_out_count " +
            "  FROM ums_member_recharge_log r " +
            "  LEFT JOIN sys_payment_method pm2 ON pm2.code = r.pay_method COLLATE utf8mb4_general_ci AND pm2.tenant_id = r.tenant_id AND pm2.deleted = 0 " +
            "  WHERE r.tenant_id = #{tenantId} " +
            "    AND r.pay_method IS NOT NULL AND r.pay_method != '' " +
            ") t " +
            "GROUP BY payment_method, payment_method_name " +
            "ORDER BY total_inflow DESC" +
            "</script>")
    List<PaymentMethodStatVO> selectPaymentMethodStats(@Param("tenantId") Long tenantId);

    @Select("<script>" +
            "SELECT " +
            "  pay_date, " +
            "  payment_method, " +
            "  payment_method_name, " +
            "  SUM(inflow) AS inflow, " +
            "  SUM(outflow) AS outflow, " +
            "  SUM(inflow_count) AS inflow_count, " +
            "  SUM(outflow_count) AS outflow_count " +
            "FROM (" +
            "  SELECT " +
            "    DATE(pay_time) AS pay_date, " +
            "    op.payment_method, " +
            "    COALESCE(pm.name, op.payment_method) AS payment_method_name, " +
            "    SUM(CASE WHEN op.amount &gt; 0 THEN op.amount ELSE 0 END) AS inflow, " +
            "    SUM(CASE WHEN op.amount &lt; 0 THEN ABS(op.amount) ELSE 0 END) AS outflow, " +
            "    SUM(CASE WHEN op.amount &gt; 0 THEN 1 ELSE 0 END) AS inflow_count, " +
            "    SUM(CASE WHEN op.amount &lt; 0 THEN 1 ELSE 0 END) AS outflow_count " +
            "  FROM oms_order_payment op " +
            "  LEFT JOIN sys_payment_method pm ON pm.code = op.payment_method COLLATE utf8mb4_general_ci AND pm.tenant_id = op.tenant_id AND pm.deleted = 0 " +
            "  WHERE op.tenant_id = #{tenantId} AND op.deleted = 0 " +
            "    AND op.pay_time &gt;= #{startDate} AND op.pay_time &lt; #{endDate} " +
            "    AND op.payment_method != 'stored' " +
            "  GROUP BY DATE(pay_time), op.payment_method, pm.name " +
            "  UNION ALL " +
            "  SELECT " +
            "    DATE(r.create_time) AS pay_date, " +
            "    r.pay_method COLLATE utf8mb4_general_ci AS payment_method, " +
            "    COALESCE(pm2.name, r.pay_method) COLLATE utf8mb4_general_ci AS payment_method_name, " +
            "    r.amount AS inflow, " +
            "    0 AS outflow, " +
            "    1 AS inflow_count, " +
            "    0 AS outflow_count " +
            "  FROM ums_member_recharge_log r " +
            "  LEFT JOIN sys_payment_method pm2 ON pm2.code = r.pay_method COLLATE utf8mb4_general_ci AND pm2.tenant_id = r.tenant_id AND pm2.deleted = 0 " +
            "  WHERE r.tenant_id = #{tenantId} " +
            "    AND r.create_time &gt;= #{startDate} AND r.create_time &lt; #{endDate} " +
            "    AND r.pay_method IS NOT NULL AND r.pay_method != '' " +
            ") t " +
            "GROUP BY pay_date, payment_method, payment_method_name " +
            "ORDER BY pay_date ASC, payment_method ASC" +
            "</script>")
    List<DailyPaymentFlowVO> selectDailyPaymentFlow(
            @Param("tenantId") Long tenantId,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate
    );
}
