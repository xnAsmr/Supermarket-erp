package com.supermarket.erp.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.erp.module.system.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    @Select("SELECT * FROM sys_menu WHERE parent_id = 0 AND deleted = 0 ORDER BY sort")
    List<Menu> selectRootMenus();

    @Select("SELECT * FROM sys_menu WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort")
    List<Menu> selectByParentId(@Param("parentId") Long parentId);

    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id AND rm.deleted = 0 " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.deleted = 0 AND m.status = 1 AND m.menu_type IN (0, 1) " +
            "ORDER BY m.sort")
    List<Menu> selectMenusByUserId(@Param("userId") Long userId);

    @Select("SELECT DISTINCT m.permission FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id AND rm.deleted = 0 " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.deleted = 0 AND m.status = 1 " +
            "AND m.menu_type = 2 AND m.permission IS NOT NULL AND m.permission != ''")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}
