package net.cocotea.cyreneadmin.util;

import cn.hutool.crypto.SmUtil;
import lombok.RequiredArgsConstructor;
import net.cocotea.cyreneadmin.properties.AppSystemProp;

/**
 * 安全工具类
 *
 * @author CoCoTea
 * @date 2022-3-30 16:21:21
 */
@RequiredArgsConstructor
public class SecurityUtils {

    private final AppSystemProp appSystemProp;

    /**
     * 获取密码密文
     *
     * @param password 原始密码
     * @return 密文
     */
    public String getPwd(String password) {
        return SmUtil
                .sm3WithSalt(appSystemProp.getPasswordSalt().getBytes())
                .digestHex(password)
                .toUpperCase();
    }

}
