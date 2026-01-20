package net.cocotea.cyreneadmin.service;

import cn.hutool.core.lang.tree.Tree;
import net.cocotea.cyreneadmin.model.dto.SysDictionaryAddDTO;
import net.cocotea.cyreneadmin.model.dto.SysDictionaryPageDTO;
import net.cocotea.cyreneadmin.model.dto.SysDictionaryTreeDTO;
import net.cocotea.cyreneadmin.model.dto.SysDictionaryUpdateDTO;
import net.cocotea.cyreneadmin.model.vo.SysDictionaryVO;
import net.cocotea.cyreneadmin.model.ApiPage;

import java.math.BigInteger;
import java.util.List;

/**
 * 字典 接口服务类
 * @author CoCoTea
 * @version 2.0.0
 */
public interface SysDictionaryService extends BaseService<ApiPage<SysDictionaryVO>, SysDictionaryPageDTO, SysDictionaryAddDTO, SysDictionaryUpdateDTO> {

    /**
     * 获取树形结构
     * @param param 分页参数
     * @return 分页对象
     */
    List<Tree<BigInteger>> listByTree(SysDictionaryTreeDTO param);
}
