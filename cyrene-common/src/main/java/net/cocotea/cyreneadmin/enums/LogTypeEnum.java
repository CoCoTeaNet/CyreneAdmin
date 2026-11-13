package net.cocotea.cyreneadmin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作类型枚举值（显示标签见字典 log_type）
 *
 * @author CoCoTea
 * @version v1.0.0
 */
@Getter
@AllArgsConstructor
public enum LogTypeEnum {

    LOGIN(1),
    OPERATION(2);

    final Integer code;
}
