package com.supermarket.erp.module.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.erp.module.stock.entity.StockOutItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface StockOutItemMapper extends BaseMapper<StockOutItem> {

    @Select("SELECT * FROM wms_stock_out_item WHERE out_id = #{outId}")
    List<StockOutItem> selectByOutId(@Param("outId") Long outId);

    @Delete("DELETE FROM wms_stock_out_item WHERE out_id = #{outId}")
    int deleteByOutId(@Param("outId") Long outId);
}
