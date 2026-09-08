package com.supermarket.erp.module.payment.service.impl;

import com.supermarket.erp.common.enums.PaymentMethodEnum;
import com.supermarket.erp.module.payment.service.IPaymentService;
import com.supermarket.erp.module.payment.vo.PaymentMethodVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentServiceImpl implements IPaymentService {

    @Override
    public List<PaymentMethodVO> getPaymentMethods() {
        List<PaymentMethodVO> list = new ArrayList<>();
        for (PaymentMethodEnum method : PaymentMethodEnum.values()) {
            PaymentMethodVO vo = new PaymentMethodVO();
            vo.setCode(method.getCode());
            vo.setName(method.getName());
            vo.setIcon(method.name().toLowerCase());
            list.add(vo);
        }
        return list;
    }
}
