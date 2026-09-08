package com.supermarket.erp.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.erp.module.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    @Select("SELECT * FROM pms_product WHERE barcode = #{barcode} AND deleted = 0 AND tenant_id = #{tenantId}")
    Product selectByBarcode(@Param("barcode") String barcode, @Param("tenantId") Long tenantId);

    @Select("SELECT * FROM pms_product WHERE tenant_id = #{tenantId} AND deleted = 0 " +
            "AND (name LIKE CONCAT('%',#{keyword},'%') OR pinyin LIKE CONCAT('%',#{keyword},'%') OR barcode = #{keyword})")
    List<Product> searchByKeyword(@Param("keyword") String keyword, @Param("tenantId") Long tenantId);
}
