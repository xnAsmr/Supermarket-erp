package com.supermarket.erp.module.member.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.supermarket.erp.common.annotation.LogOperation;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.common.util.SecurityUtil;
import com.supermarket.erp.module.auth.entity.User;
import com.supermarket.erp.module.auth.mapper.UserMapper;
import com.supermarket.erp.module.member.dto.MemberCreateDTO;
import com.supermarket.erp.module.member.dto.MemberQueryDTO;
import com.supermarket.erp.module.member.entity.Member;
import com.supermarket.erp.module.member.entity.MemberRechargeLog;
import com.supermarket.erp.module.member.mapper.MemberMapper;
import com.supermarket.erp.module.member.mapper.MemberRechargeLogMapper;
import com.supermarket.erp.module.member.service.IMemberService;
import com.supermarket.erp.module.member.vo.MemberConsumeLogVO;
import com.supermarket.erp.module.member.vo.MemberVO;
import com.supermarket.erp.module.order.entity.Order;
import com.supermarket.erp.module.order.entity.OrderPayment;
import com.supermarket.erp.module.order.mapper.OrderMapper;
import com.supermarket.erp.module.order.mapper.OrderPaymentMapper;
import com.supermarket.erp.module.system.entity.PaymentMethod;
import com.supermarket.erp.module.system.mapper.PaymentMethodMapper;
import com.supermarket.erp.module.tenant.entity.Store;
import com.supermarket.erp.module.tenant.mapper.StoreMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "会员管理")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final IMemberService memberService;
    private final MemberMapper memberMapper;
    private final MemberRechargeLogMapper memberRechargeLogMapper;
    private final UserMapper userMapper;
    private final PaymentMethodMapper paymentMethodMapper;
    private final OrderMapper orderMapper;
    private final OrderPaymentMapper orderPaymentMapper;
    private final StoreMapper storeMapper;

    @Operation(summary = "分页查询会员列表")
    @GetMapping
    public Result<PageResult<MemberVO>> pageList(MemberQueryDTO query) {
        Long tenantId = SecurityUtil.getTenantId();
        query.setTenantId(tenantId);
        return Result.success(memberService.pageList(query));
    }

    @Operation(summary = "获取会员详情")
    @GetMapping("/{id}")
    public Result<MemberVO> getById(@PathVariable Long id) {
        return Result.success(memberService.getById(id));
    }

    @Operation(summary = "新增会员")
    @LogOperation(module = "会员管理", operation = "新增会员")
    @PreAuthorize("@perm.check('ums:member:add')")
    @PostMapping
    public Result<MemberVO> create(@RequestBody @Valid MemberCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        return Result.success(memberService.create(dto, tenantId, userId));
    }

    @Operation(summary = "修改会员信息")
    @LogOperation(module = "会员管理", operation = "修改会员")
    @PreAuthorize("@perm.check('ums:member:edit')")
    @PutMapping("/{id}")
    public Result<MemberVO> update(@PathVariable Long id, @RequestBody @Valid MemberCreateDTO dto) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(memberService.update(id, dto, tenantId));
    }

    @Operation(summary = "删除会员")
    @LogOperation(module = "会员管理", operation = "删除会员")
    @PreAuthorize("@perm.check('ums:member:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Long tenantId = SecurityUtil.getTenantId();
        memberService.delete(id, tenantId);
        return Result.success();
    }

    @Operation(summary = "会员充值")
    @LogOperation(module = "会员管理", operation = "会员充值")
    @PreAuthorize("@perm.check('ums:member:recharge')")
    @PutMapping("/{id}/recharge")
    @Transactional
    public Result<Void> recharge(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long tenantId = SecurityUtil.getTenantId();
        Long userId = SecurityUtil.getUserId();
        BigDecimal amount = new BigDecimal(body.get("amount").toString());
        BigDecimal giftAmount = body.get("giftAmount") != null ? new BigDecimal(body.get("giftAmount").toString()) : BigDecimal.ZERO;
        String payMethod = body.get("payMethod") != null ? body.get("payMethod").toString() : null;
        String remark = (String) body.getOrDefault("remark", "");

        Member member = memberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException("会员不存在");
        }

        BigDecimal beforeBalance = member.getBalance();
        BigDecimal afterBalance = beforeBalance.add(amount).add(giftAmount);

        memberMapper.update(null, new LambdaUpdateWrapper<Member>()
                .eq(Member::getId, member.getId())
                .set(Member::getBalance, afterBalance)
        );

        // 更新支付方式余额（储值卡充值，支付方式收到现金）
        if (payMethod != null && !payMethod.isEmpty()) {
            PaymentMethod pm = paymentMethodMapper.selectOne(
                    new LambdaQueryWrapper<PaymentMethod>()
                            .eq(PaymentMethod::getTenantId, tenantId)
                            .eq(PaymentMethod::getCode, payMethod)
                            .eq(PaymentMethod::getDeleted, 0)
            );
            if (pm != null && pm.getBalance() != null) {
                pm.setBalance(pm.getBalance().add(amount));
                paymentMethodMapper.updateById(pm);
            }
        }

        User operator = userMapper.selectById(userId);
        MemberRechargeLog log = new MemberRechargeLog();
        log.setTenantId(tenantId);
        log.setMemberId(member.getId());
        log.setMemberName(member.getName());
        log.setType(1);
        log.setAmount(amount);
        log.setGiftAmount(giftAmount);
        log.setBeforeBalance(beforeBalance);
        log.setAfterBalance(afterBalance);
        log.setPayMethod(payMethod);
        log.setOperatorId(userId);
        log.setOperatorName(operator != null ? operator.getUsername() : "");
        log.setRemark(remark);
        memberRechargeLogMapper.insert(log);

        return Result.success();
    }

    @Operation(summary = "查询会员充值/消费记录")
    @GetMapping("/{id}/recharge-log")
    public Result<PageResult<MemberRechargeLog>> rechargeLog(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long tenantId = SecurityUtil.getTenantId();
        LambdaQueryWrapper<MemberRechargeLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberRechargeLog::getTenantId, tenantId)
                .eq(MemberRechargeLog::getMemberId, id)
                .orderByDesc(MemberRechargeLog::getCreateTime);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<MemberRechargeLog> p =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        var result = memberRechargeLogMapper.selectPage(p, wrapper);
        return Result.success(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Operation(summary = "查询会员消费记录（储值卡扣减）")
    @GetMapping("/{id}/consume-log")
    public Result<PageResult<MemberConsumeLogVO>> consumeLog(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        Long tenantId = SecurityUtil.getTenantId();

        // 查询该会员使用储值卡支付的订单
        LambdaQueryWrapper<Order> orderWrapper = new LambdaQueryWrapper<>();
        orderWrapper.eq(Order::getTenantId, tenantId)
                .eq(Order::getMemberId, id)
                .eq(Order::getPayMethod, "stored")
                .eq(Order::getDeleted, 0)
                .orderByDesc(Order::getOrderTime);
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Order> orderPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
        var orderResult = orderMapper.selectPage(orderPage, orderWrapper);

        List<MemberConsumeLogVO> voList = orderResult.getRecords().stream().map(order -> {
            MemberConsumeLogVO vo = new MemberConsumeLogVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setAmount(order.getTotalAmount());
            vo.setPayAmount(order.getPayAmount());
            vo.setPayMethod(order.getPayMethod());
            vo.setPayMethodName("储值卡");
            vo.setOrderStatus(order.getOrderStatus());
            vo.setOrderStatusName(getOrderStatusName(order.getOrderStatus()));
            vo.setOrderTime(order.getOrderTime());

            // 查询门店名称
            if (order.getStoreId() != null) {
                Store store = storeMapper.selectById(order.getStoreId());
                if (store != null) vo.setStoreName(store.getStoreName());
            }

            return vo;
        }).collect(Collectors.toList());

        return Result.success(PageResult.of(voList, orderResult.getTotal(), page, pageSize));
    }

    private String getOrderStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待支付";
            case 1: return "已支付";
            case 2: return "已完成";
            case 3: return "已退款";
            case 4: return "已取消";
            default: return "未知";
        }
    }

    private String getPayMethodName(String code) {
        if (code == null) return "未知";
        switch (code) {
            case "cash": return "现金";
            case "wechat": return "微信";
            case "alipay": return "支付宝";
            case "card": return "银行卡";
            case "stored": return "储值卡";
            default: return code;
        }
    }

    @Operation(summary = "根据手机号查询会员(POS使用)")
    @GetMapping("/by-phone/{phone}")
    public Result<MemberVO> getByPhone(@PathVariable String phone) {
        Long tenantId = SecurityUtil.getTenantId();
        return Result.success(memberService.getByPhone(phone, tenantId));
    }
}
