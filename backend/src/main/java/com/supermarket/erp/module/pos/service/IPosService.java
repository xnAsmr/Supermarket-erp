package com.supermarket.erp.module.pos.service;

import com.supermarket.erp.module.pos.dto.CheckoutDTO;
import com.supermarket.erp.module.pos.dto.CheckoutVO;

public interface IPosService {

    CheckoutVO checkout(CheckoutDTO dto, Long tenantId, Long userId);
}
