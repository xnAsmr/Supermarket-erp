package com.supermarket.erp.module.system.service.impl;

import com.supermarket.erp.common.exception.BusinessException;
import com.supermarket.erp.module.system.entity.Dict;
import com.supermarket.erp.module.system.mapper.DictMapper;
import com.supermarket.erp.module.system.service.IDictService;
import com.supermarket.erp.module.system.vo.DictVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DictServiceImpl implements IDictService {

    private final DictMapper dictMapper;

    @Override
    public List<DictVO> getByDictType(String dictType) {
        List<Dict> dicts = dictMapper.selectByDictType(dictType);
        return dicts.stream().map(this::toVO).collect(Collectors.toList());
    }

    @Override
    public Long create(Dict dict) {
        if (dict.getSort() == null) {
            dict.setSort(0);
        }
        if (dict.getStatus() == null) {
            dict.setStatus(1);
        }
        dictMapper.insert(dict);
        return dict.getId();
    }

    @Override
    public void update(Long id, Dict dict) {
        Dict existing = dictMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("字典不存在");
        }
        dict.setId(id);
        dictMapper.updateById(dict);
    }

    @Override
    public void delete(Long id) {
        Dict existing = dictMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("字典不存在");
        }
        dictMapper.deleteById(id);
    }

    private DictVO toVO(Dict dict) {
        DictVO vo = new DictVO();
        BeanUtils.copyProperties(dict, vo);
        return vo;
    }
}
