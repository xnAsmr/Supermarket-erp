package com.supermarket.erp.module.system.service;

import com.supermarket.erp.module.system.entity.Dict;
import com.supermarket.erp.module.system.vo.DictVO;

import java.util.List;

public interface IDictService {

    List<DictVO> getByDictType(String dictType);

    Long create(Dict dict);

    void update(Long id, Dict dict);

    void delete(Long id);
}
