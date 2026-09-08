package com.supermarket.erp.module.pos.controller;

import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.pos.dto.CheckoutDTO;
import com.supermarket.erp.module.pos.dto.CheckoutVO;
import com.supermarket.erp.module.pos.service.IPosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "POS收银")
@RestController
@RequestMapping("/api/v1/pos")
@RequiredArgsConstructor
public class PosController {

    private final IPosService posService;

    @Operation(summary = "收银结账")
    @PostMapping("/checkout")
    public Result<CheckoutVO> checkout(@Valid @RequestBody CheckoutDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        return Result.success(posService.checkout(dto, tenantId, userId));
    }
}
