package com.supermarket.erp.module.pos.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.supermarket.erp.common.enums.OrderStatusEnum;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.module.member.entity.Member;
import com.supermarket.erp.module.member.entity.MemberLevel;
import com.supermarket.erp.module.member.mapper.MemberLevelMapper;
import com.supermarket.erp.module.member.mapper.MemberMapper;
import com.supermarket.erp.module.order.entity.Order;
import com.supermarket.erp.module.order.entity.OrderItem;
import com.supermarket.erp.module.order.entity.OrderPayment;
import com.supermarket.erp.module.order.mapper.OrderItemMapper;
import com.supermarket.erp.module.order.mapper.OrderMapper;
import com.supermarket.erp.module.order.mapper.OrderPaymentMapper;
import com.supermarket.erp.module.order.vo.OrderItemVO;
import com.supermarket.erp.module.pos.dto.CheckoutDTO;
import com.supermarket.erp.module.pos.dto.CheckoutVO;
import com.supermarket.erp.module.pos.service.IPosService;
import com.supermarket.erp.module.product.entity.Product;
import com.supermarket.erp.module.product.mapper.ProductMapper;
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
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
@Transactional
public class PosServiceImpl implements IPosService {

    private final ProductMapper productMapper;
    private final StockMapper stockMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderPaymentMapper orderPaymentMapper;
    private final StockLogMapper stockLogMapper;
    private final MemberMapper memberMapper;
    private final MemberLevelMapper memberLevelMapper;
    private final PaymentMethodMapper paymentMethodMapper;

    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    @Override
    public CheckoutVO checkout(CheckoutDTO dto, Long tenantId, Long userId) {
        List<CheckoutDTO.CheckoutItemDTO> items = dto.getItems();
        List<CheckoutDTO.PaymentDetailDTO> payments = dto.getPayments();

        List<Product> productList = new ArrayList<>();
        List<Stock> stockList = new ArrayList<>();

        for (CheckoutDTO.CheckoutItemDTO item : items) {
            Product product = productMapper.selectById(item.getProductId());
            if (product == null) {
                throw new BusinessException("商品不存在: " + item.getProductId());
            }
            productList.add(product);

            LambdaQueryWrapper<Stock> stockWrapper = new LambdaQueryWrapper<>();
            stockWrapper.eq(Stock::getProductId, item.getProductId())
                    .eq(Stock::getStoreId, dto.getStoreId())
                    .eq(Stock::getTenantId, tenantId);
            Stock stock = stockMapper.selectOne(stockWrapper);
            if (stock == null) {
                throw new BusinessException("商品库存不存在: " + product.getName());
            }
            BigDecimal shelfQty = stock.getShelfQuantity() != null ? stock.getShelfQuantity() : BigDecimal.ZERO;
            if (shelfQty.compareTo(BigDecimal.valueOf(item.getQuantity())) < 0) {
                throw new BusinessException("货架库存不足: " + product.getName()
                        + "，货架" + shelfQty + "，需要" + item.getQuantity() + "。请先从仓库补货到货架。");
            }
            stockList.add(stock);
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (int i = 0; i < items.size(); i++) {
            CheckoutDTO.CheckoutItemDTO item = items.get(i);
            BigDecimal itemTotal = item.getSalePrice().multiply(new BigDecimal(item.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        BigDecimal payAmount = BigDecimal.ZERO;
        for (CheckoutDTO.PaymentDetailDTO payment : payments) {
            payAmount = payAmount.add(payment.getAmount());
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        Member member = null;
        MemberLevel memberLevel = null;
        if (dto.getMemberId() != null) {
            member = memberMapper.selectById(dto.getMemberId());
            if (member != null) {
                memberLevel = memberLevelMapper.selectById(member.getLevelId());
                if (memberLevel != null && memberLevel.getDiscount() != null
                        && memberLevel.getDiscount().compareTo(BigDecimal.ONE) < 0) {
                    discountAmount = totalAmount.subtract(totalAmount.multiply(memberLevel.getDiscount()).setScale(2, RoundingMode.HALF_UP));
                }
            }
        }

        if (payAmount.compareTo(totalAmount.subtract(discountAmount)) < 0) {
            throw new BusinessException("支付金额不足");
        }
        BigDecimal changeAmount = payAmount.subtract(totalAmount.subtract(discountAmount));

        String orderNo = generateOrderNo(dto.getStoreId());

        Order order = new Order();
        order.setTenantId(tenantId);
        order.setStoreId(dto.getStoreId());
        order.setOrderNo(orderNo);
        order.setOrderType(1);
        order.setOrderStatus(payAmount.compareTo(BigDecimal.ZERO) > 0
                ? OrderStatusEnum.COMPLETED.getCode() : OrderStatusEnum.PENDING.getCode());
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(discountAmount);
        order.setPayAmount(totalAmount.subtract(discountAmount));
        order.setChangeAmount(changeAmount);
        order.setPayStatus(payAmount.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
        if (payments.size() == 1) {
            order.setPayMethod(payments.get(0).getPaymentMethod());
            order.setIsCombinedPay(0);
        } else {
            order.setPayMethod("combined");
            order.setIsCombinedPay(1);
        }
        if (dto.getMemberId() != null) {
            order.setMemberId(dto.getMemberId());
        }
        order.setOperatorId(userId);
        order.setOrderTime(LocalDateTime.now());
        if (payAmount.compareTo(BigDecimal.ZERO) > 0) {
            order.setPayTime(LocalDateTime.now());
            order.setCompleteTime(LocalDateTime.now());
        }
        orderMapper.insert(order);

        List<OrderItemVO> orderItemVOs = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            CheckoutDTO.CheckoutItemDTO item = items.get(i);
            Product product = productList.get(i);
            Stock stock = stockList.get(i);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setProductId(product.getId());
            orderItem.setProductName(product.getName());
            orderItem.setBarcode(product.getBarcode());
            orderItem.setSpec(product.getSpec());
            orderItem.setUnit(product.getUnit());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setSalePrice(item.getSalePrice());
            orderItem.setTotalPrice(item.getSalePrice().multiply(new BigDecimal(item.getQuantity())));
            orderItem.setCostAmount(product.getPurchasePrice() != null ? product.getPurchasePrice() : BigDecimal.ZERO);
            orderItem.setIsWeight(item.getIsWeight());
            orderItemMapper.insert(orderItem);

            OrderItemVO itemVO = new OrderItemVO();
            BeanUtils.copyProperties(orderItem, itemVO);
            orderItemVOs.add(itemVO);
        }

        BigDecimal totalProfit = BigDecimal.ZERO;
        for (OrderItemVO itemVO : orderItemVOs) {
            BigDecimal salePrice = (itemVO.getDiscountPrice() != null && itemVO.getDiscountPrice().compareTo(BigDecimal.ZERO) > 0)
                    ? itemVO.getDiscountPrice() : itemVO.getSalePrice();
            BigDecimal costPrice = itemVO.getCostAmount() != null ? itemVO.getCostAmount() : BigDecimal.ZERO;
            totalProfit = totalProfit.add(salePrice.subtract(costPrice).multiply(new BigDecimal(itemVO.getQuantity())));
        }
        order.setProfitAmount(totalProfit);
        orderMapper.updateById(order);

        for (CheckoutDTO.PaymentDetailDTO payment : payments) {
            OrderPayment orderPayment = new OrderPayment();
            orderPayment.setTenantId(tenantId);
            orderPayment.setStoreId(dto.getStoreId());
            orderPayment.setOrderId(order.getId());
            orderPayment.setPaymentMethod(payment.getPaymentMethod());
            orderPayment.setAmount(payment.getAmount());
            orderPayment.setReceivedAmount(payment.getReceivedAmount());
            orderPayment.setPayTime(LocalDateTime.now());
            orderPaymentMapper.insert(orderPayment);

            // 储值卡支付：扣减会员余额（不更新PaymentMethod余额，因为储值卡是会员预存的钱）
            if ("stored".equals(payment.getPaymentMethod())) {
                if (member == null) {
                    throw new BusinessException("储值卡支付需要选择会员");
                }
                BigDecimal memberBalance = member.getBalance() != null ? member.getBalance() : BigDecimal.ZERO;
                if (memberBalance.compareTo(payment.getAmount()) < 0) {
                    throw new BusinessException("会员储值卡余额不足，当前余额: " + memberBalance);
                }
                memberMapper.update(null, new LambdaUpdateWrapper<Member>()
                        .eq(Member::getId, member.getId())
                        .set(Member::getBalance, memberBalance.subtract(payment.getAmount()))
                );
                member.setBalance(memberBalance.subtract(payment.getAmount()));
            } else {
                // 其他支付方式：更新PaymentMethod余额
                PaymentMethod pm = paymentMethodMapper.selectOne(
                        new LambdaQueryWrapper<PaymentMethod>()
                                .eq(PaymentMethod::getTenantId, tenantId)
                                .eq(PaymentMethod::getCode, payment.getPaymentMethod())
                                .eq(PaymentMethod::getDeleted, 0)
                );
                if (pm != null && pm.getBalance() != null) {
                    pm.setBalance(pm.getBalance().add(payment.getAmount()));
                    paymentMethodMapper.updateById(pm);
                }
            }
        }

        for (int i = 0; i < items.size(); i++) {
            CheckoutDTO.CheckoutItemDTO item = items.get(i);
            Stock stock = stockList.get(i);

            BigDecimal beforeShelfQty = stock.getShelfQuantity() != null ? stock.getShelfQuantity() : BigDecimal.ZERO;
            BigDecimal deductQty = BigDecimal.valueOf(item.getQuantity());
            int deductResult = stockMapper.deductStock(item.getProductId(), dto.getStoreId(), deductQty);
            if (deductResult == 0) {
                throw new BusinessException("货架库存扣减失败，可能库存不足: " + item.getProductId());
            }

            StockLog stockLog = new StockLog();
            stockLog.setTenantId(tenantId);
            stockLog.setStoreId(dto.getStoreId());
            stockLog.setProductId(item.getProductId());
            stockLog.setLogType(4);
            stockLog.setQuantity(deductQty);
            stockLog.setBeforeStock(beforeShelfQty);
            stockLog.setAfterStock(beforeShelfQty.subtract(deductQty));
            stockLog.setRelatedId(order.getId());
            stockLog.setOperatorId(userId);
            stockLogMapper.insert(stockLog);
        }

        if (member != null) {
            memberMapper.update(null, new LambdaUpdateWrapper<Member>()
                    .eq(Member::getId, member.getId())
                    .set(Member::getTotalConsume, member.getTotalConsume().add(totalAmount.subtract(discountAmount)))
                    .set(Member::getTotalPoints, member.getTotalPoints() + totalAmount.subtract(discountAmount).intValue())
            );
        }

        CheckoutVO vo = new CheckoutVO();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setTotalAmount(totalAmount);
        vo.setDiscountAmount(discountAmount);
        vo.setPayAmount(order.getPayAmount());
        vo.setChangeAmount(changeAmount);
        vo.setItems(orderItemVOs);
        return vo;
    }

    private String generateOrderNo(Long storeId) {
        String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        long seq = SEQUENCE.incrementAndGet() % 10000;
        return timestamp + String.format("%03d", storeId % 1000) + String.format("%04d", seq);
    }
}
