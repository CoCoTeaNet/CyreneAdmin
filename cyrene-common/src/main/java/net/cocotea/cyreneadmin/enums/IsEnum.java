package net.cocotea.cyreneadmin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 常用枚举之是否枚举（显示标签见字典 yes_no）
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Getter
@AllArgsConstructor
public enum IsEnum {

    N(0),
    Y(1);

    final Integer code;

}
