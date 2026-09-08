package com.supermarket.erp.module.tenant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.erp.module.tenant.entity.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StoreMapper extends BaseMapper<Store> {

    @Select("SELECT * FROM sys_store WHERE tenant_id = #{tenantId} AND deleted = 0")
    List<Store> selectByTenantId(@Param("tenantId") Long tenantId);
}
