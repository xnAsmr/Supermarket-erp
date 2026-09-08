package com.supermarket.erp.module.member.service;

import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.member.dto.MemberCreateDTO;
import com.supermarket.erp.module.member.dto.MemberQueryDTO;
import com.supermarket.erp.module.member.vo.MemberVO;

public interface IMemberService {

    MemberVO getById(Long id);

    PageResult<MemberVO> pageList(MemberQueryDTO query);

    MemberVO getByPhone(String phone, Long tenantId);

    MemberVO create(MemberCreateDTO dto, Long tenantId, Long userId);

    MemberVO update(Long id, MemberCreateDTO dto, Long tenantId);

    void delete(Long id, Long tenantId);
}
