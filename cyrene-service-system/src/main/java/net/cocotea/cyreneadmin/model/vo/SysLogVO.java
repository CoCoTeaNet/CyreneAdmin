package net.cocotea.cyreneadmin.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import net.cocotea.cyreneadmin.enums.LogStatusEnum;
import net.cocotea.cyreneadmin.enums.LogTypeEnum;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class SysLogVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 4883072904594356224L;

    /**
     * 日志编号
     */
    private String id;

    /**
     * ip地址
     */
    private String ipAddress;

    /**
     * 操作人
     */
    private String operator;

    /**
     * 操作人名称
     */
    private String username;

    /**
     * 操作人昵称
     */
    private String nickname;

    /**
     * 请求方法
     */
    private String requestWay;

    /**
     * 接口路径
     */
    private String apiPath;

    /**
     * 日志状态; {@link LogStatusEnum}
     */
    private Integer logStatus;

    /**
     * 日志类型：{@link LogTypeEnum}
     */
    private Integer logType;

    /**
     * 操作时间
     */
    private LocalDateTime createTime;

}
