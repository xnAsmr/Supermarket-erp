package com.supermarket.erp.module.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.erp.module.stock.entity.StockOut;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StockOutMapper extends BaseMapper<StockOut> {

    @Select("SELECT * FROM wms_stock_out WHERE id = #{id} AND deleted = 0")
    StockOut selectByIdAndNotDeleted(@Param("id") Long id);
}
