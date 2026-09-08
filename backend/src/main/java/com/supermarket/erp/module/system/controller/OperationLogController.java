package com.supermarket.erp.module.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.system.entity.OperationLog;
import com.supermarket.erp.module.system.mapper.OperationLogMapper;
import com.supermarket.erp.module.tenant.entity.Tenant;
import com.supermarket.erp.module.tenant.mapper.TenantMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/v1/operation-logs")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogMapper operationLogMapper;
    private final TenantMapper tenantMapper;

    @Operation(summary = "操作日志分页列表")
    @GetMapping
    public Result<PageResult<Map<String, Object>>> list(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long tenantId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Integer userType = SecurityUtil.getLoginUser().getUserType();
        boolean isSystemAdmin = userType != null && userType == 3;

        LambdaQueryWrapper<OperationLog> wrapper = new LambdaQueryWrapper<>();
        if (isSystemAdmin) {
            if (tenantId != null) {
                wrapper.eq(OperationLog::getTenantId, tenantId);
            }
        } else {
            wrapper.eq(OperationLog::getTenantId, SecurityUtil.getTenantId());
        }
        if (StringUtils.hasText(module)) {
            wrapper.like(OperationLog::getModule, module);
        }
        if (StringUtils.hasText(username)) {
            wrapper.like(OperationLog::getUsername, username);
        }
        if (status != null) {
            wrapper.eq(OperationLog::getStatus, status);
        }
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(OperationLog::getCreateTime, startDate + " 00:00:00");
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(OperationLog::getCreateTime, endDate + " 23:59:59");
        }
        wrapper.orderByDesc(OperationLog::getCreateTime);

        Page<OperationLog> pageResult = operationLogMapper.selectPage(new Page<>(page, pageSize), wrapper);

        // 为系统管理员补充租户名称
        Map<Long, String> tenantNameMap = new java.util.HashMap<>();
        if (isSystemAdmin) {
            Set<Long> tenantIds = pageResult.getRecords().stream()
                    .map(OperationLog::getTenantId)
                    .filter(java.util.Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!tenantIds.isEmpty()) {
                List<Tenant> tenants = tenantMapper.selectBatchIds(tenantIds);
                tenantNameMap = tenants.stream()
                        .collect(Collectors.toMap(Tenant::getId, Tenant::getTenantName));
            }
        }

        final Map<Long, String> nameMap = tenantNameMap;
        List<Map<String, Object>> voList = pageResult.getRecords().stream().map(log -> {
            Map<String, Object> vo = new LinkedHashMap<>();
            vo.put("id", log.getId());
            vo.put("tenantId", log.getTenantId());
            vo.put("tenantName", nameMap.getOrDefault(log.getTenantId(), null));
            vo.put("module", log.getModule());
            vo.put("operation", log.getOperation());
            vo.put("method", log.getMethod());
            vo.put("url", log.getUrl());
            vo.put("status", log.getStatus());
            vo.put("errorMsg", log.getErrorMsg());
            vo.put("ip", log.getIp());
            vo.put("userId", log.getUserId());
            vo.put("username", log.getUsername());
            vo.put("duration", log.getDuration());
            vo.put("createTime", log.getCreateTime());
            return vo;
        }).collect(Collectors.toList());

        return Result.success(new PageResult<>(voList, pageResult.getTotal(), page, pageSize));
    }
}
