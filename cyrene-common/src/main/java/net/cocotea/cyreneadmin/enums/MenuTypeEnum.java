package net.cocotea.cyreneadmin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 按钮类型;0目录 1菜单 2按钮（显示标签见字典 menu_type）
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum {
    DIRECTORY(0),
    MENU(1),
    BUTTON(2);

    final Integer code;
}
