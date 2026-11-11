package net.cocotea.cyreneadmin.config;

import net.cocotea.cyreneadmin.autoddl.AutoDdlExecutor;
import org.noear.solon.Solon;
import org.noear.solon.core.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.List;

/**
 * Solon 启动期执行 auto-ddl。
 *
 * @author CoCoTea
 */
public final class AutoDdlBootstrap {

    private static final Logger logger = LoggerFactory.getLogger(AutoDdlBootstrap.class);

    private static final String DEFAULT_SCRIPTS = "classpath:db/ddl.sql,classpath:db/init-data.sql,classpath:db/init-menu-zh.sql";

    private AutoDdlBootstrap() {
    }

    public static void run(AppContext context) {
        if (!Solon.cfg().getBool("myapp.auto-ddl.enabled", false)) {
            return;
        }
        String scripts = Solon.cfg().get("myapp.auto-ddl.scripts", DEFAULT_SCRIPTS);
        List<String> locations = Arrays.stream(scripts.split(","))
                .map(String::trim)
                .filter(location -> !location.isEmpty())
                .toList();
        if (locations.isEmpty()) {
            logger.warn("auto-ddl 已启用但未配置脚本");
            return;
        }
        AutoDdlExecutor.execute(context.getBean(DataSource.class), locations);
    }
}
