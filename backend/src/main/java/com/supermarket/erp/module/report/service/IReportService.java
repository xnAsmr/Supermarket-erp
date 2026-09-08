package com.supermarket.erp.module.report.service;

import com.supermarket.erp.module.report.vo.DailyReportVO;
import com.supermarket.erp.module.report.vo.DashboardVO;
import com.supermarket.erp.module.report.vo.MemberReportVO;
import com.supermarket.erp.module.report.vo.MonthlyReportVO;
import com.supermarket.erp.module.report.vo.StockCategoryReportVO;

import java.time.LocalDate;
import java.util.List;

public interface IReportService {

    DailyReportVO getDailyReport(Long tenantId, Long storeId, LocalDate date);

    MonthlyReportVO getMonthlyReport(Long tenantId, Long storeId, Integer year, Integer month);

    DashboardVO getDashboard(Long tenantId, Long storeId);

    List<StockCategoryReportVO> getStockCategoryReport(Long tenantId);

    MemberReportVO getMemberReport(Long tenantId, Integer year, Integer month);
}
