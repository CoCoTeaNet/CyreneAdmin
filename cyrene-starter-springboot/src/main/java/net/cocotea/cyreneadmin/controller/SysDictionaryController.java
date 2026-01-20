package net.cocotea.cyreneadmin.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import net.cocotea.cyreneadmin.model.dto.SysDictionaryAddDTO;
import net.cocotea.cyreneadmin.model.dto.SysDictionaryTreeDTO;
import net.cocotea.cyreneadmin.model.dto.SysDictionaryUpdateDTO;
import net.cocotea.cyreneadmin.model.vo.SysDictionaryVO;
import net.cocotea.cyreneadmin.service.SysDictionaryService;
import net.cocotea.cyreneadmin.model.ApiResult;
import net.cocotea.cyreneadmin.model.BusinessException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;
import java.util.List;

/**
 * 系统字典管理接口
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@RestController
@RequestMapping("/system/dictionary")
public class SysDictionaryController {
    @Resource
    private SysDictionaryService sysDictionaryService;

    /**
     * 新增字典
     *
     * @param dictionaryAddDTO {@link SysDictionaryAddDTO}
     * @return 成功返回true
     * @throws BusinessException 业务异常
     */
    @PostMapping("/add")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> add(@Valid @RequestBody SysDictionaryAddDTO dictionaryAddDTO) throws BusinessException {
        boolean b = sysDictionaryService.add(dictionaryAddDTO);
        return ApiResult.ok(b);
    }

    /**
     * 批量删除
     *
     * @param list 字典主键ID集合
     * @return 成功返回true
     * @throws BusinessException 业务异常
     */
    @PostMapping("/deleteBatch")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> deleteBatch(@Valid @RequestBody List<BigInteger> list) throws BusinessException {
        boolean b = sysDictionaryService.deleteBatch(list);
        return ApiResult.ok(b);
    }

    /**
     * 更新字典信息
     *
     * @param param {@link SysDictionaryUpdateDTO}
     * @return 成功返回true
     * @throws BusinessException 业务异常
     */
    @PostMapping("/update")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<Boolean> update(@Valid @RequestBody SysDictionaryUpdateDTO param) throws BusinessException {
        boolean b = sysDictionaryService.update(param);
        return ApiResult.ok(b);
    }

    /**
     * 获取字典树形列表
     *
     * @param sysDictionaryTreeDTO {@link SysDictionaryTreeDTO}
     * @return {@link SysDictionaryVO}
     */
    @PostMapping("/listByTree")
    @SaCheckRole(value = {"role:super:admin", "role:simple:admin"}, mode = SaMode.OR)
    public ApiResult<List<Tree<BigInteger>>> listByTree(@Valid @RequestBody SysDictionaryTreeDTO sysDictionaryTreeDTO) {
        return ApiResult.ok(sysDictionaryService.listByTree(sysDictionaryTreeDTO));
    }
}
