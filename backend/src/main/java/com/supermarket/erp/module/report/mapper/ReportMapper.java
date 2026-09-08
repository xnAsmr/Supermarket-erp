package com.supermarket.erp.module.report.mapper;

import com.supermarket.erp.module.report.vo.DailyReportVO;
import com.supermarket.erp.module.report.vo.MemberReportVO;
import com.supermarket.erp.module.report.vo.MonthlyReportVO;
import com.supermarket.erp.module.report.vo.StockCategoryReportVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ReportMapper {

    @Select("<script>" +
            "SELECT " +
            "#{statDate} AS statDate, " +
            "IFNULL(SUM(o.pay_amount),0) AS payAmount, " +
            "IFNULL(SUM(o.total_amount),0) AS totalAmount, " +
            "IFNULL(SUM(o.discount_amount),0) AS discountAmount, " +
            "COUNT(*) AS orderCount, " +
            "IFNULL(SUM(o.profit_amount),0) AS profitAmount, " +
            "(SELECT COUNT(*) FROM ums_member m WHERE m.tenant_id = #{tenantId} AND m.deleted = 0 AND DATE(m.create_time) = #{statDate}) AS memberCount " +
            "FROM oms_order o " +
            "WHERE o.tenant_id = #{tenantId} AND o.deleted = 0 " +
            "AND DATE(o.create_time) = #{statDate} " +
            "<if test='storeId != null'>AND o.store_id = #{storeId}</if>" +
            "</script>")
    DailyReportVO selectDailyReport(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId, @Param("statDate") LocalDate statDate);

    @Select("<script>" +
            "SELECT " +
            "IFNULL(SUM(o.pay_amount),0) AS payAmount, " +
            "IFNULL(SUM(o.total_amount),0) AS totalAmount, " +
            "IFNULL(SUM(o.discount_amount),0) AS discountAmount, " +
            "IFNULL(COUNT(*),0) AS orderCount, " +
            "IFNULL(SUM(o.profit_amount),0) AS profitAmount " +
            "FROM oms_order o " +
            "WHERE o.tenant_id = #{tenantId} AND o.deleted = 0 " +
            "AND YEAR(o.create_time) = #{year} AND MONTH(o.create_time) = #{month} " +
            "<if test='storeId != null'>AND o.store_id = #{storeId}</if>" +
            "</script>")
    MonthlyReportVO selectMonthlyReport(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId, @Param("year") Integer year, @Param("month") Integer month);

    @Select("<script>" +
            "SELECT " +
            "c.id, " +
            "c.name AS categoryName, " +
            "COUNT(DISTINCT p.id) AS productCount, " +
            "IFNULL(SUM(s.shelf_quantity + s.warehouse_quantity), 0) AS totalStock, " +
            "IFNULL(SUM((s.shelf_quantity + s.warehouse_quantity) * p.purchase_price), 0) AS totalValue, " +
            "SUM(CASE WHEN s.shelf_quantity &lt; 10 THEN 1 ELSE 0 END) AS lowStockCount, " +
            "0 AS monthlyInValue, " +
            "0 AS monthlyOutValue " +
            "FROM pms_category c " +
            "LEFT JOIN pms_product p ON p.category_id = c.id AND p.tenant_id = #{tenantId} AND p.deleted = 0 " +
            "LEFT JOIN wms_stock s ON s.product_id = p.id AND s.tenant_id = #{tenantId} AND s.deleted = 0 " +
            "WHERE c.tenant_id = #{tenantId} AND c.deleted = 0 " +
            "GROUP BY c.id, c.name " +
            "ORDER BY totalValue DESC" +
            "</script>")
    List<StockCategoryReportVO> selectStockCategoryReport(@Param("tenantId") Long tenantId);

    @Select("<script>" +
            "SELECT " +
            "(SELECT COUNT(*) FROM ums_member WHERE tenant_id = #{tenantId} AND deleted = 0) AS totalMembers, " +
            "(SELECT COUNT(*) FROM ums_member WHERE tenant_id = #{tenantId} AND deleted = 0 AND YEAR(create_time) = #{year} AND MONTH(create_time) = #{month}) AS newMembersThisMonth, " +
            "(SELECT COUNT(DISTINCT o.member_id) FROM oms_order o WHERE o.tenant_id = #{tenantId} AND o.deleted = 0 AND o.member_id IS NOT NULL AND YEAR(o.create_time) = #{year} AND MONTH(o.create_time) = #{month}) AS activeMembers, " +
            "(SELECT IFNULL(SUM(balance), 0) FROM ums_member WHERE tenant_id = #{tenantId} AND deleted = 0) AS totalBalance, " +
            "(SELECT IFNULL(SUM(total_consume), 0) FROM ums_member WHERE tenant_id = #{tenantId} AND deleted = 0) AS totalConsume" +
            "</script>")
    MemberReportVO selectMemberReport(@Param("tenantId") Long tenantId, @Param("year") Integer year, @Param("month") Integer month);
}
