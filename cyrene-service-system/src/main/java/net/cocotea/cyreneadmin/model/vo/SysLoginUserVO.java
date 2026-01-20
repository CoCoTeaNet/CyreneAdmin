package net.cocotea.cyreneadmin.model.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

/**
 * 登录用户视图对象
 * 用于封装用户登录后的基本信息和权限数据
 *
 * @author CoCoTea
 * @version 1.0.0
 * @project sss-rbac-admin
 * @description sys_user, 系统用户表
 */
@Data
@Accessors(chain = true)
public class SysLoginUserVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1641777558644610990L;

    /**
     * 登录状态
     * 表示用户当前的登录状态，true表示已登录，false表示未登录
     */
    private Boolean loginStatus;

    /**
     * 用户ID
     * 系统中用户的唯一标识符
     */
    private BigInteger id;

    /**
     * 用户名
     * 用户登录时使用的用户名
     */
    private String username;

    /**
     * 昵称
     * 用户的显示昵称
     */
    private String nickname;

    /**
     * 头像
     * 用户头像的存储路径或URL
     */
    private String avatar;

    /**
     * 认证令牌
     * 用户登录成功后生成的认证令牌，用于后续请求的身份验证
     */
    private String token;

}