package com.supermarket.erp.module.system.controller;

import com.supermarket.erp.common.result.Result;
import com.supermarket.erp.module.system.entity.Dict;
import com.supermarket.erp.module.system.service.IDictService;
import com.supermarket.erp.module.system.vo.DictVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "数据字典")
@RestController
@RequestMapping("/api/v1/dicts")
@RequiredArgsConstructor
public class DictController {

    private final IDictService dictService;

    @Operation(summary = "按字典类型查询")
    @GetMapping("/type/{dictType}")
    public Result<List<DictVO>> getByType(@PathVariable String dictType) {
        return Result.success(dictService.getByDictType(dictType));
    }

    @Operation(summary = "新增字典")
    @PostMapping
    public Result<Long> create(@RequestBody Dict dict) {
        return Result.success(dictService.create(dict));
    }

    @Operation(summary = "修改字典")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Dict dict) {
        dictService.update(id, dict);
        return Result.success();
    }

    @Operation(summary = "删除字典")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dictService.delete(id);
        return Result.success();
    }
}
