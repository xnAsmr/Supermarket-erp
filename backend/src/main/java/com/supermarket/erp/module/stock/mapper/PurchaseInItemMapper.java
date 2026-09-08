package com.supermarket.erp.module.stock.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.erp.module.stock.entity.PurchaseInItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface PurchaseInItemMapper extends BaseMapper<PurchaseInItem> {

    @Select("SELECT * FROM wms_purchase_in_item WHERE in_id = #{inId}")
    List<PurchaseInItem> selectByInId(@Param("inId") Long inId);

    @Delete("DELETE FROM wms_purchase_in_item WHERE in_id = #{inId}")
    int deleteByInId(@Param("inId") Long inId);
}
