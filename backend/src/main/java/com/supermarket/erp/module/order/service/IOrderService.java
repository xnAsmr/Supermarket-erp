package com.supermarket.erp.module.order.service;

import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.order.dto.OrderQueryDTO;
import com.supermarket.erp.module.order.entity.OrderItem;
import com.supermarket.erp.module.order.vo.OrderVO;

import java.util.List;

public interface IOrderService {

    OrderVO getById(Long id, Long tenantId);

    PageResult<OrderVO> pageList(OrderQueryDTO query, Long tenantId);

    void cancel(Long id, Long tenantId, Long userId);

    void complete(Long id, Long tenantId, Long userId);

    void refund(Long id, Long tenantId, Long userId);

    void delete(Long id, Long tenantId, Long userId);

    void updateRemark(Long id, Long tenantId, String remark, Long userId);

    List<OrderItem> getOrderItems(Long orderId);
}
