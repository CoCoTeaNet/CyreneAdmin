package net.cocotea.cyreneadmin.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.cyreneadmin.model.ApiPageDTO;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.io.Serializable;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysMenuPageDTO extends ApiPageDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -772057092053351688L;

    /**
     * 详细查询条件
     */
    @NotNull(message = "查询参数为空")
    private Query sysMenu;

    @Data
    public static class Query {
        /**
         * 是否菜单
         * {@link net.cocotea.cyreneadmin.enums.IsEnum}
         */
        private Integer isMenu;

        /**
         * 权限编号
         */
        private String permissionCode;

        /**
         * 菜单名称
         */
        private String menuName;

        /**
         * 菜单状态
         * {@link net.cocotea.cyreneadmin.enums.MenuStatusEnum}
         */
        private Integer menuStatus;
    }

}
