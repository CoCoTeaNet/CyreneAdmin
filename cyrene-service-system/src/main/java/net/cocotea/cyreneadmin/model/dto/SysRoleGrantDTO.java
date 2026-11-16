package net.cocotea.cyreneadmin.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 角色授权入参：按菜单/权限分区替换，menuIds 为空表示清空该分区
 *
 * @author CoCoTea
 */
@Data
@Accessors(chain = true)
public class SysRoleGrantDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "角色ID不能为空")
    private String roleId;

    /**
     * 分区：1菜单 0权限按钮，与 sys_menu.is_menu 一致
     */
    @NotNull(message = "菜单类型不能为空")
    private Integer isMenu;

    private List<String> menuIds;
}