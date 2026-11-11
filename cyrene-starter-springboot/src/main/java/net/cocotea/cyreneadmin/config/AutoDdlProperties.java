package net.cocotea.cyreneadmin.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * auto-ddl 配置
 *
 * @author CoCoTea
 */
@Data
public class AutoDdlProperties {

    /**
     * 是否在启动时自动执行建表与初始化数据
     */
    private boolean enabled = false;

    /**
     * 脚本路径，按顺序执行，支持 classpath: 与 file: 前缀
     */
    private List<String> scripts = new ArrayList<>(List.of(
            "classpath:db/ddl.sql",
            "classpath:db/init-data.sql",
            "classpath:db/init-menu-zh.sql"
    ));
}
