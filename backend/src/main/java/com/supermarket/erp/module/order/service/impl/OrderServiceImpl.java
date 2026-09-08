package com.supermarket.erp.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.order.dto.OrderQueryDTO;
import com.supermarket.erp.module.order.entity.Order;
import com.supermarket.erp.module.order.entity.OrderItem;
import com.supermarket.erp.module.order.entity.OrderPayment;
import com.supermarket.erp.module.order.mapper.OrderItemMapper;
import com.supermarket.erp.module.order.mapper.OrderMapper;
import com.supermarket.erp.module.order.mapper.OrderPaymentMapper;
import com.supermarket.erp.module.order.service.IOrderService;
import com.supermarket.erp.module.order.vo.OrderItemVO;
import com.supermarket.erp.module.order.vo.OrderVO;
import com.supermarket.erp.module.auth.entity.User;
import com.supermarket.erp.module.auth.mapper.UserMapper;
import com.supermarket.erp.module.member.entity.Member;
import com.supermarket.erp.module.member.mapper.MemberMapper;
import com.supermarket.erp.module.stock.entity.Stock;
import com.supermarket.erp.module.stock.entity.StockLog;
import com.supermarket.erp.module.stock.mapper.StockLogMapper;
import com.supermarket.erp.module.stock.mapper.StockMapper;
import com.supermarket.erp.module.system.entity.PaymentMethod;
import com.supermarket.erp.module.system.mapper.PaymentMethodMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements IOrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderPaymentMapper orderPaymentMapper;
    private final UserMapper userMapper;
    private final MemberMapper memberMapper;
    private final PaymentMethodMapper paymentMethodMapper;
    private final StockMapper stockMapper;
    private final StockLogMapper stockLogMapper;

    @Override
    public OrderVO getById(Long id, Long tenantId) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, id)
                        .eq(Order::getTenantId, tenantId)
                        .eq(Order::getDeleted, 0)
        );
        if (order == null) {
            throw new BusinessException(4001, "订单不存在");
        }

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);

        if (order.getPayMethod() != null) {
            PaymentMethod pm = paymentMethodMapper.selectOne(
                    new LambdaQueryWrapper<PaymentMethod>()
                            .eq(PaymentMethod::getCode, order.getPayMethod())
                            .eq(PaymentMethod::getTenantId, tenantId)
                            .eq(PaymentMethod::getDeleted, 0)
            );
            if (pm != null) {
                vo.setPayMethodName(pm.getName());
            }
        }

        if (order.getOperatorId() != null) {
            User user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getId, order.getOperatorId())
                            .eq(User::getDeleted, 0)
            );
            if (user != null) {
                vo.setCashierName(user.getName() != null ? user.getName() : user.getUsername());
            }
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(id);
        List<OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            BigDecimal salePrice = (item.getDiscountPrice() != null && item.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0)
                    ? item.getDiscountPrice() : item.getSalePrice();
            BigDecimal costPrice = item.getCostAmount() != null ? item.getCostAmount() : BigDecimal.ZERO;
            itemVO.setProfitAmount(salePrice.subtract(costPrice).multiply(new BigDecimal(item.getQuantity())));
            return itemVO;
        }).collect(Collectors.toList());
        vo.setItems(itemVOs);

        return vo;
    }

    @Override
    public PageResult<OrderVO> pageList(OrderQueryDTO query, Long tenantId) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getTenantId, tenantId);
        wrapper.eq(Order::getDeleted, 0);

        if (query.getKeyword() != null && !query.getKeyword().isEmpty()) {
            wrapper.like(Order::getOrderNo, query.getKeyword());
        }
        if (query.getOrderStatus() != null) {
            wrapper.eq(Order::getOrderStatus, query.getOrderStatus());
        }
        if (query.getPayStatus() != null) {
            wrapper.eq(Order::getPayStatus, query.getPayStatus());
        }
        if (query.getOperatorId() != null) {
            wrapper.eq(Order::getOperatorId, query.getOperatorId());
        }
        if (query.getStartDate() != null) {
            wrapper.ge(Order::getCreateTime, query.getStartDate());
        }
        if (query.getEndDate() != null) {
            wrapper.le(Order::getCreateTime, query.getEndDate());
        }

        wrapper.orderByDesc(Order::getCreateTime);

        Page<Order> page = new Page<>(query.getPage(), query.getPageSize());
        Page<Order> result = orderMapper.selectPage(page, wrapper);

        List<OrderVO> voList = result.getRecords().stream().map(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);

            if (order.getOperatorId() != null) {
                User cashier = userMapper.selectById(order.getOperatorId());
                if (cashier != null) {
                    vo.setCashierName(cashier.getName() != null ? cashier.getName() : cashier.getUsername());
                }
            }

            if (order.getPayMethod() != null && !order.getPayMethod().isEmpty()) {
                PaymentMethod pm = paymentMethodMapper.selectOne(
                        new LambdaQueryWrapper<PaymentMethod>()
                                .eq(PaymentMethod::getTenantId, tenantId)
                                .eq(PaymentMethod::getCode, order.getPayMethod())
                );
                if (pm != null) {
                    vo.setPayMethodName(pm.getName());
                }
                if ("combined".equals(order.getPayMethod())) {
                    vo.setPayMethodName("组合支付");
                }
            }

            if (order.getMemberId() != null) {
                Member member = memberMapper.selectById(order.getMemberId());
                if (member != null) {
                    vo.setMemberName(member.getName());
                }
            }

            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
            List<OrderItemVO> itemVOs = items.stream().map(item -> {
                OrderItemVO itemVO = new OrderItemVO();
                BeanUtils.copyProperties(item, itemVO);
                return itemVO;
            }).collect(Collectors.toList());
            vo.setItems(itemVOs);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(voList, result.getTotal(), query.getPage(), query.getPageSize());
    }

    @Override
    @Transactional
    public void cancel(Long id, Long tenantId, Long userId) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, id)
                        .eq(Order::getTenantId, tenantId)
        );
        if (order == null) {
            throw new BusinessException(4001, "订单不存在");
        }
        if (order.getOrderStatus() != 0) {
            throw new BusinessException(4002, "只有待支付订单才能取消");
        }

        order.setOrderStatus(3);
        order.setUpdateBy(userId);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void complete(Long id, Long tenantId, Long userId) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, id)
                        .eq(Order::getTenantId, tenantId)
        );
        if (order == null) {
            throw new BusinessException(4001, "订单不存在");
        }
        if (order.getOrderStatus() != 1) {
            throw new BusinessException(4002, "只有已支付订单才能完成");
        }

        order.setOrderStatus(2);
        order.setCompleteTime(LocalDateTime.now());
        order.setUpdateBy(userId);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void refund(Long id, Long tenantId, Long userId) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, id)
                        .eq(Order::getTenantId, tenantId)
        );
        if (order == null) {
            throw new BusinessException(4001, "订单不存在");
        }
        if (order.getOrderStatus() != 1 && order.getOrderStatus() != 2) {
            throw new BusinessException(4002, "只有已支付或已完成订单才能退款");
        }

        List<OrderItem> items = orderItemMapper.selectByOrderId(id);
        for (OrderItem item : items) {
            Stock stock = stockMapper.selectByProduct(tenantId, order.getStoreId(), item.getProductId());
            if (stock == null) {
                throw new BusinessException("库存记录不存在: " + item.getProductName());
            }
            BigDecimal beforeShelfQty = stock.getShelfQuantity() != null ? stock.getShelfQuantity() : BigDecimal.ZERO;
            BigDecimal restoreQty = BigDecimal.valueOf(item.getQuantity());
            stockMapper.restoreStock(item.getProductId(), order.getStoreId(), restoreQty);

            StockLog stockLog = new StockLog();
            stockLog.setTenantId(tenantId);
            stockLog.setStoreId(order.getStoreId());
            stockLog.setProductId(item.getProductId());
            stockLog.setLogType(6);
            stockLog.setQuantity(restoreQty);
            stockLog.setBeforeStock(beforeShelfQty);
            stockLog.setAfterStock(beforeShelfQty.add(restoreQty));
            stockLog.setRelatedId(order.getId());
            stockLog.setOperatorId(userId);
            stockLogMapper.insert(stockLog);
        }

        if (order.getMemberId() != null) {
            Member member = memberMapper.selectById(order.getMemberId());
            if (member != null) {
                memberMapper.update(null, new LambdaUpdateWrapper<Member>()
                        .eq(Member::getId, member.getId())
                        .set(Member::getTotalConsume, member.getTotalConsume().subtract(order.getPayAmount()))
                        .set(Member::getTotalPoints, member.getTotalPoints() - order.getPayAmount().intValue())
                );
            }
        }

        List<OrderPayment> orderPayments = orderPaymentMapper.selectList(
                new LambdaQueryWrapper<OrderPayment>()
                        .eq(OrderPayment::getOrderId, id)
        );
        for (OrderPayment op : orderPayments) {
            PaymentMethod pm = paymentMethodMapper.selectOne(
                    new LambdaQueryWrapper<PaymentMethod>()
                            .eq(PaymentMethod::getTenantId, tenantId)
                            .eq(PaymentMethod::getCode, op.getPaymentMethod())
                            .eq(PaymentMethod::getDeleted, 0)
            );
            if (pm != null) {
                pm.setBalance(pm.getBalance().subtract(op.getAmount()));
                paymentMethodMapper.updateById(pm);
            }
        }

        order.setOrderStatus(4);
        order.setPayStatus(3);
        order.setUpdateBy(userId);
        orderMapper.updateById(order);
    }

    @Override
    @Transactional
    public void delete(Long id, Long tenantId, Long userId) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, id)
                        .eq(Order::getTenantId, tenantId)
        );
        if (order == null) {
            throw new BusinessException(4001, "订单不存在");
        }

        if (order.getOrderStatus() == 1 || order.getOrderStatus() == 2) {
            List<OrderItem> items = orderItemMapper.selectByOrderId(id);
            for (OrderItem item : items) {
                Stock stock = stockMapper.selectByProduct(tenantId, order.getStoreId(), item.getProductId());
                if (stock != null) {
                    BigDecimal beforeShelfQty = stock.getShelfQuantity() != null ? stock.getShelfQuantity() : BigDecimal.ZERO;
                    BigDecimal restoreQty = BigDecimal.valueOf(item.getQuantity());
                    stockMapper.restoreStock(item.getProductId(), order.getStoreId(), restoreQty);

                    StockLog stockLog = new StockLog();
                    stockLog.setTenantId(tenantId);
                    stockLog.setStoreId(order.getStoreId());
                    stockLog.setProductId(item.getProductId());
                    stockLog.setLogType(6);
                    stockLog.setQuantity(restoreQty);
                    stockLog.setBeforeStock(beforeShelfQty);
                    stockLog.setAfterStock(beforeShelfQty.add(restoreQty));
                    stockLog.setRelatedId(order.getId());
                    stockLog.setOperatorId(userId);
                    stockLogMapper.insert(stockLog);
                }
            }

            if (order.getMemberId() != null && order.getPayAmount() != null) {
                Member member = memberMapper.selectById(order.getMemberId());
                if (member != null) {
                    memberMapper.update(null, new LambdaUpdateWrapper<Member>()
                            .eq(Member::getId, member.getId())
                            .set(Member::getTotalConsume, member.getTotalConsume().subtract(order.getPayAmount()))
                            .set(Member::getTotalPoints, member.getTotalPoints() - order.getPayAmount().intValue())
                    );
                }
            }
        }

        orderMapper.deleteById(order.getId());
    }

    @Override
    public void updateRemark(Long id, Long tenantId, String remark, Long userId) {
        Order order = orderMapper.selectOne(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getId, id)
                        .eq(Order::getTenantId, tenantId)
        );
        if (order == null) {
            throw new BusinessException(4001, "订单不存在");
        }

        order.setRemark(remark);
        order.setUpdateBy(userId);
        orderMapper.updateById(order);
    }

    @Override
    public List<OrderItem> getOrderItems(Long orderId) {
        return orderItemMapper.selectByOrderId(orderId);
    }
}
