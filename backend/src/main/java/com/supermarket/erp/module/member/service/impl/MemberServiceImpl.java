package com.supermarket.erp.module.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.common.result.PageResult;
import com.supermarket.erp.module.member.dto.MemberCreateDTO;
import com.supermarket.erp.module.member.dto.MemberQueryDTO;
import com.supermarket.erp.module.member.entity.Member;
import com.supermarket.erp.module.member.entity.MemberLevel;
import com.supermarket.erp.module.member.mapper.MemberLevelMapper;
import com.supermarket.erp.module.member.mapper.MemberMapper;
import com.supermarket.erp.module.member.service.IMemberService;
import com.supermarket.erp.module.member.vo.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements IMemberService {

    private final MemberMapper memberMapper;
    private final MemberLevelMapper memberLevelMapper;

    @Override
    public MemberVO getById(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException(5001, "会员不存在");
        }
        MemberVO vo = toVO(member);
        fillLevelName(vo);
        return vo;
    }

    @Override
    public PageResult<MemberVO> pageList(MemberQueryDTO query) {
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();

        if (query.getTenantId() != null) {
            wrapper.eq(Member::getTenantId, query.getTenantId());
        }

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(Member::getName, query.getKeyword())
                    .or().like(Member::getPhone, query.getKeyword()));
        }
        if (query.getLevelId() != null) {
            wrapper.eq(Member::getLevelId, query.getLevelId());
        }
        if (query.getStatus() != null) {
            wrapper.eq(Member::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(Member::getCreateTime);

        Page<Member> page = new Page<>(query.getPage(), query.getPageSize());
        Page<Member> result = memberMapper.selectPage(page, wrapper);

        List<MemberLevel> levels = memberLevelMapper.selectList(null);
        Map<Long, String> levelMap = levels.stream()
                .collect(Collectors.toMap(MemberLevel::getId, MemberLevel::getName));

        List<MemberVO> voList = result.getRecords().stream().map(m -> {
            MemberVO vo = toVO(m);
            vo.setLevelName(levelMap.get(m.getLevelId()));
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(voList, result.getTotal(), (int) result.getCurrent(), (int) result.getSize());
    }

    @Override
    public MemberVO getByPhone(String phone, Long tenantId) {
        Member member = memberMapper.selectByPhone(phone, tenantId);
        if (member == null) {
            throw new BusinessException(5001, "会员不存在");
        }
        MemberVO vo = toVO(member);
        fillLevelName(vo);
        return vo;
    }

    @Override
    public MemberVO create(MemberCreateDTO dto, Long tenantId, Long userId) {
        Member existing = memberMapper.selectByPhone(dto.getPhone(), tenantId);
        if (existing != null) {
            throw new BusinessException(5002, "手机号已被注册");
        }

        Member member = new Member();
        BeanUtils.copyProperties(dto, member);
        member.setTenantId(tenantId);
        member.setMemberNo("M" + System.currentTimeMillis());
        member.setLevelId(dto.getLevelId() != null ? dto.getLevelId() : 1L);
        member.setPoints(0);
        member.setBalance(BigDecimal.ZERO);
        member.setTotalConsume(BigDecimal.ZERO);
        member.setTotalPoints(0);
        member.setStatus(1);
        member.setRegisterTime(LocalDateTime.now());
        member.setCreateBy(userId);
        member.setUpdateBy(userId);
        memberMapper.insert(member);
        return toVO(member);
    }

    @Override
    public MemberVO update(Long id, MemberCreateDTO dto, Long tenantId) {
        Member member = memberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException(5001, "会员不存在");
        }

        if (!dto.getPhone().equals(member.getPhone())) {
            Member existing = memberMapper.selectByPhone(dto.getPhone(), tenantId);
            if (existing != null) {
                throw new BusinessException(5002, "手机号已被注册");
            }
        }

        member.setName(dto.getName());
        member.setPhone(dto.getPhone());
        member.setGender(dto.getGender());
        member.setBirthday(dto.getBirthday());
        if (dto.getLevelId() != null) {
            member.setLevelId(dto.getLevelId());
        }
        member.setEmail(dto.getEmail());
        member.setAddress(dto.getAddress());
        memberMapper.updateById(member);
        MemberVO vo = toVO(member);
        fillLevelName(vo);
        return vo;
    }

    @Override
    public void delete(Long id, Long tenantId) {
        Member member = memberMapper.selectById(id);
        if (member == null) {
            throw new BusinessException(5001, "会员不存在");
        }
        memberMapper.deleteById(id);
    }

    private MemberVO toVO(Member member) {
        MemberVO vo = new MemberVO();
        BeanUtils.copyProperties(member, vo);
        return vo;
    }

    private void fillLevelName(MemberVO vo) {
        if (vo.getLevelId() != null) {
            MemberLevel level = memberLevelMapper.selectById(vo.getLevelId());
            if (level != null) {
                vo.setLevelName(level.getName());
            }
        }
    }
}
