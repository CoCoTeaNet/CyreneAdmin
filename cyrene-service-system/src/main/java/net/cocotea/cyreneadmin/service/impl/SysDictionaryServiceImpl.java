package net.cocotea.cyreneadmin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNode;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.core.text.CharPool;
import lombok.RequiredArgsConstructor;
import net.cocotea.cyreneadmin.model.dto.SysDictionaryAddDTO;
import net.cocotea.cyreneadmin.model.dto.SysDictionaryPageDTO;
import net.cocotea.cyreneadmin.model.dto.SysDictionaryTreeDTO;
import net.cocotea.cyreneadmin.model.dto.SysDictionaryUpdateDTO;
import net.cocotea.cyreneadmin.model.po.SysDictionary;
import net.cocotea.cyreneadmin.model.po.SysUser;
import net.cocotea.cyreneadmin.model.vo.SysDictionaryVO;
import net.cocotea.cyreneadmin.service.SysDictionaryService;
import net.cocotea.cyreneadmin.enums.IsEnum;
import net.cocotea.cyreneadmin.model.ApiPage;
import org.sagacity.sqltoy.dao.LightDao;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class SysDictionaryServiceImpl implements SysDictionaryService {

    private final LightDao lightDao;

    @Override
    public boolean add(SysDictionaryAddDTO addDTO) {
        SysDictionary sysDictionary = lightDao.convertType(addDTO, SysDictionary.class);
        if (sysDictionary.getParentId() == null) {
            sysDictionary.setParentId(BigInteger.ZERO);
        }
        if (sysDictionary.getSort() == null) {
            sysDictionary.setSort(0);
        }
        Object o = lightDao.save(sysDictionary);
        return o != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(SysDictionaryUpdateDTO param) {
        SysDictionary sysDictionary = lightDao.convertType(param, SysDictionary.class);
        Long update = lightDao.update(sysDictionary);
        return update != null;
    }

    @Override
    public ApiPage<SysDictionaryVO> listByPage(SysDictionaryPageDTO dto) {
        return null;
    }

    @Override
    public List<Tree<BigInteger>> listByTree(SysDictionaryTreeDTO dictionaryTreeDTO) {
        SysDictionaryVO dictionaryVO = new SysDictionaryVO()
                .setDictionaryName(dictionaryTreeDTO.getDictionaryName())
                .setEnableStatus(dictionaryTreeDTO.getEnableStatus());
        List<TreeNode<BigInteger>> nodeList = findNodeList(dictionaryVO);
        return TreeUtil.build(nodeList, BigInteger.ZERO);
    }

    private List<TreeNode<BigInteger>> findNodeList(SysDictionaryVO sysDictionaryVO) {
        List<SysDictionary> dictionaryList = lightDao.find("sys_dictionary_findList", BeanUtil.toBean(sysDictionaryVO, SysDictionary.class), SysDictionary.class);
        // 查询关联的用户名称
        List<BigInteger> userIds = dictionaryList.stream().map(SysDictionary::getCreateBy).collect(Collectors.toList());
        List<SysUser> sysUsers = lightDao.loadByIds(SysUser.class, userIds);
        Map<BigInteger, String> userMap = sysUsers
                .stream()
                .collect(Collectors.toMap(SysUser::getId, i -> i.getUsername().concat(String.valueOf(CharPool.AT)).concat(i.getNickname())));
        // 创建树节点
        List<TreeNode<BigInteger>> nodeList = new ArrayList<>(dictionaryList.size());
        for (SysDictionary dictionary : dictionaryList) {
            Map<String, Object> extraMap = BeanUtil.beanToMap(dictionary);
            if (dictionary.getCreateBy() != null) {
                String username = userMap.get(dictionary.getCreateBy());
                extraMap.put("createBy", username);
            }
            TreeNode<BigInteger> node = new TreeNode<>(dictionary.getId(), dictionary.getParentId(), dictionary.getDictionaryName(), dictionary.getSort());
            node.setExtra(extraMap);
            nodeList.add(node);
        }
        return nodeList;
    }

    @Override
    public boolean delete(BigInteger id) {
        SysDictionary sysDictionary = new SysDictionary().setId(id).setIsDeleted(IsEnum.Y.getCode());
        Long update = lightDao.update(sysDictionary);
        if (update <= 0) {
            return false;
        }
        // 获取子节点
        List<SysDictionary> list = lightDao.find("sys_dictionary_findList", new SysDictionary().setParentId(id), SysDictionary.class);
        if (!list.isEmpty()) {
            // 存在子节点，删除子节点
            list.forEach(item -> delete(item.getId()));
        }
        return true;
    }

}
