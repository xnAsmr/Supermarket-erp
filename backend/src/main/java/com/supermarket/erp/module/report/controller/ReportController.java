package com.supermarket.erp.module.report.controller;

import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.report.service.IReportService;
import com.supermarket.erp.module.report.vo.DailyReportVO;
import com.supermarket.erp.module.report.vo.DashboardVO;
import com.supermarket.erp.module.report.vo.MemberReportVO;
import com.supermarket.erp.module.report.vo.MonthlyReportVO;
import com.supermarket.erp.module.report.vo.StockCategoryReportVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "报表统计")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final IReportService reportService;

    @Operation(summary = "日报")
    @GetMapping("/daily")
    public Result<DailyReportVO> dailyReport(@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        Long tenantId = SecurityUtil.getTenantId();
        Long storeId = SecurityUtil.getStoreId();
        return Result.success(reportService.getDailyReport(tenantId, storeId, date));
    }

    @Operation(summary = "月报")
    @GetMapping("/monthly")
    public Result<MonthlyReportVO> monthlyReport(@RequestParam Integer year, @RequestParam Integer month) {
        Long tenantId = SecurityUtil.getTenantId();
        Long storeId = SecurityUtil.getStoreId();
        return Result.success(reportService.getMonthlyReport(tenantId, storeId, year, month));
    }

    @Operation(summary = "仪表盘")
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        Long tenantId = SecurityUtil.getTenantId();
        Long storeId = SecurityUtil.getStoreId();
        return Result.success(reportService.getDashboard(tenantId, storeId));
    }

    @Operation(summary = "库存分类报表")
    @GetMapping("/stock-category")
    public Result<List<StockCategoryReportVO>> stockCategoryReport() {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(reportService.getStockCategoryReport(tenantId));
    }

    @Operation(summary = "会员报表")
    @GetMapping("/member")
    public Result<MemberReportVO> memberReport(@RequestParam Integer year, @RequestParam Integer month) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(reportService.getMemberReport(tenantId, year, month));
    }
}
