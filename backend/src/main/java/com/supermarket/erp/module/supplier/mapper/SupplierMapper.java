package com.supermarket.erp.module.supplier.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.erp.module.supplier.entity.Supplier;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SupplierMapper extends BaseMapper<Supplier> {

    @Select("SELECT * FROM pms_supplier WHERE supplier_no = #{supplierNo} AND deleted = 0 AND tenant_id = #{tenantId}")
    Supplier selectBySupplierNo(@Param("supplierNo") String supplierNo, @Param("tenantId") Long tenantId);
}
