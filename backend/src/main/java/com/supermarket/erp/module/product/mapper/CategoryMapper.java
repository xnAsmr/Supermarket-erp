package com.supermarket.erp.module.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.erp.module.product.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT * FROM pms_category WHERE tenant_id = #{tenantId} AND deleted = 0 AND parent_id = #{parentId} ORDER BY sort")
    List<Category> selectByParentId(@Param("parentId") Long parentId, @Param("tenantId") Long tenantId);

    @Select("SELECT * FROM pms_category WHERE tenant_id = #{tenantId} AND deleted = 0 AND level = 1 ORDER BY sort")
    List<Category> selectRootCategories(@Param("tenantId") Long tenantId);
}
