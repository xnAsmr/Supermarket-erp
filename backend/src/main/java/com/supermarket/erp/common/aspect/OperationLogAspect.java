package com.supermarket.erp.common.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.supermarket.erp.common.annotation.LogOperation;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.system.entity.OperationLog;
import com.supermarket.erp.module.system.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogMapper operationLogMapper;
    private final ObjectMapper objectMapper;

    @Around("@annotation(logOperation)")
    public Object around(ProceedingJoinPoint joinPoint, LogOperation logOperation) throws Throwable {
        long startTime = System.currentTimeMillis();
        OperationLog record = new OperationLog();
        record.setModule(logOperation.module());
        record.setOperation(logOperation.operation());
        record.setMethod(joinPoint.getSignature().toShortString());

        HttpServletRequest request = null;
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                request = attrs.getRequest();
                record.setUrl(request.getRequestURI());
                record.setIp(getClientIp(request));
            }
        } catch (Exception e) {
            // ignore
        }

        try {
            record.setUserId(SecurityUtil.getUserId());
            record.setUsername(SecurityUtil.getUsername());
            record.setTenantId(SecurityUtil.getTenantId());
        } catch (Exception e) {
            // not authenticated, e.g. login request
        }

        try {
            Object[] args = joinPoint.getArgs();
            record.setParams(serializeArgs(args));
        } catch (Exception e) {
            record.setParams("[序列化失败]");
        }

        Object result = null;
        try {
            result = joinPoint.proceed();
            record.setStatus(1);
            try {
                record.setResult(objectMapper.writeValueAsString(result));
            } catch (Exception e) {
                // ignore
            }
            return result;
        } catch (Throwable t) {
            record.setStatus(0);
            record.setErrorMsg(t.getMessage());
            throw t;
        } finally {
            record.setDuration(System.currentTimeMillis() - startTime);
            record.setCreateTime(LocalDateTime.now());
            try {
                operationLogMapper.insert(record);
            } catch (Exception e) {
                log.error("记录操作日志失败", e);
            }
        }
    }

    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(Arrays.stream(args)
                    .filter(a -> !(a instanceof HttpServletRequest))
                    .filter(a -> {
                        if (a instanceof String s) {
                            return !s.startsWith("Bearer ");
                        }
                        return true;
                    })
                    .limit(3)
                    .toList());
        } catch (Exception e) {
            return null;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
