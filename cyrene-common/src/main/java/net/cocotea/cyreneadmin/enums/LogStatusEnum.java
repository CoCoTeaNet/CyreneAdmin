package net.cocotea.cyreneadmin.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 操作状态 枚举类（显示标签见字典 log_status）
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Getter
@AllArgsConstructor
public enum LogStatusEnum {
    /**
     * 0异常 1成功
     */
    ERROR(0),
    SUCCESS(1);

    final Integer code;
}
