package com.supermarket.erp.module.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.erp.module.tenant.entity.Tenant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TenantMapper extends BaseMapper<Tenant> {

    @Select("SELECT * FROM sys_tenant WHERE tenant_no = #{tenantNo} AND deleted = 0")
    Tenant selectByTenantNo(@Param("tenantNo") String tenantNo);
}
