package com.supermarket.erp.module.member.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.erp.module.member.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {

    @Select("SELECT * FROM ums_member WHERE phone = #{phone} AND tenant_id = #{tenantId} AND deleted = 0")
    Member selectByPhone(String phone, Long tenantId);
}
