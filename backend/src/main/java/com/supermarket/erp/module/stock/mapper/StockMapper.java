package com.supermarket.erp.module.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.erp.module.stock.entity.Stock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface StockMapper extends BaseMapper<Stock> {

    @Select("SELECT * FROM wms_stock WHERE tenant_id = #{tenantId} AND store_id = #{storeId} AND product_id = #{productId} AND deleted = 0")
    Stock selectByProduct(@Param("tenantId") Long tenantId, @Param("storeId") Long storeId, @Param("productId") Long productId);

    @Update("UPDATE wms_stock SET shelf_quantity = shelf_quantity - #{quantity}, update_time = NOW() " +
            "WHERE product_id = #{productId} AND store_id = #{storeId} AND deleted = 0 AND shelf_quantity >= #{quantity}")
    int deductStock(@Param("productId") Long productId, @Param("storeId") Long storeId, @Param("quantity") BigDecimal quantity);

    @Update("UPDATE wms_stock SET shelf_quantity = shelf_quantity + #{quantity}, update_time = NOW() " +
            "WHERE product_id = #{productId} AND store_id = #{storeId} AND deleted = 0")
    int restoreStock(@Param("productId") Long productId, @Param("storeId") Long storeId, @Param("quantity") BigDecimal quantity);

    @Update("UPDATE wms_stock SET warehouse_quantity = warehouse_quantity + #{quantity}, update_time = NOW() " +
            "WHERE product_id = #{productId} AND store_id = #{storeId} AND deleted = 0")
    int inboundStock(@Param("productId") Long productId, @Param("storeId") Long storeId, @Param("quantity") BigDecimal quantity);

    @Update("UPDATE wms_stock SET warehouse_quantity = warehouse_quantity - #{quantity}, shelf_quantity = shelf_quantity + #{quantity}, update_time = NOW() " +
            "WHERE product_id = #{productId} AND store_id = #{storeId} AND deleted = 0 AND warehouse_quantity >= #{quantity}")
    int outboundStock(@Param("productId") Long productId, @Param("storeId") Long storeId, @Param("quantity") BigDecimal quantity);
}
