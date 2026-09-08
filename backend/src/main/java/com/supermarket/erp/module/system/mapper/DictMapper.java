package com.supermarket.erp.module.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.supermarket.erp.module.system.entity.Dict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DictMapper extends BaseMapper<Dict> {

    @Select("SELECT * FROM sys_dict WHERE dict_type = #{dictType} ORDER BY sort")
    List<Dict> selectByDictType(@Param("dictType") String dictType);
}
