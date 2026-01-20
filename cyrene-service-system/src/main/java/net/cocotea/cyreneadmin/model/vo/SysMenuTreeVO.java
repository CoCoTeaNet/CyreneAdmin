package net.cocotea.cyreneadmin.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

/**
 * 系统菜单树形结构视图对象
 * 用于表示系统菜单的树形结构，包含菜单的基本信息和层级关系
 */
@Data
@Accessors(chain = true)
public class SysMenuTreeVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 7261224106151023072L;

    /**
     * 菜单ID
     * 菜单的唯一标识符
     */
    private BigInteger id;
    
    /**
     * 父级菜单ID
     * 当前菜单所属的父级菜单ID，顶级菜单的父级ID通常为0或null
     */
    private BigInteger parentId;
    
    /**
     * 菜单名称
     * 菜单显示的名称
     */
    private String menuName;
    
    /**
     * 路由路径
     * 菜单对应的前端路由路径
     */
    private String routerPath;
    
    /**
     * 是否外部链接
     * 标识菜单是否为外部链接，1表示是外部链接，0表示不是
     */
    private Integer isExternalLink;
    
    /**
     * 菜单类型
     * 菜单的类型，如目录、菜单、按钮等
     */
    private Integer menuType;
    
    /**
     * 排序
     * 菜单在同级菜单中的排序位置
     */
    private Integer sort;
    
    /**
     * 图标路径
     * 菜单显示的图标路径或图标类名
     */
    private String iconPath;
    
    /**
     * 菜单状态
     * 菜单的启用状态，如启用或禁用
     */
    private Integer menuStatus;
}