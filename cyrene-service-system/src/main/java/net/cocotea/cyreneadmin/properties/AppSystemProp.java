package net.cocotea.cyreneadmin.properties;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 默认值配置项
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Accessors(chain = true)
@Data
public class AppSystemProp {

    /**
     * 默认密码
     */
    private String password;

    /**
     * 密码加密的盐
     */
    private String passwordSalt;

    /**
     * 是否开启权限缓存: true开启，false关闭
     */
    private Boolean permissionCache;

    /**
     * 是否开启系统日志保存功能
     */
    private Boolean saveLog;

    /**
     * 强密码：启用后会关闭图片验证码验证
     */
    private String strongPassword;

    /**
     * 路由放行地址
     */
    private String excludes;

}
