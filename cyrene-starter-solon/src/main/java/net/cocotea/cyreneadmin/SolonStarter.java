package net.cocotea.cyreneadmin;

import net.cocotea.cyreneadmin.constant.GlobalConst;
import net.cocotea.cyreneadmin.properties.AppSystemProp;
import org.noear.solon.Solon;
import org.noear.solon.SolonApp;
import org.noear.solon.annotation.Import;
import org.noear.solon.annotation.SolonMain;
import org.noear.solon.core.AppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author CoCoTea
 * @since v1.2.7
 */
@SolonMain
@Import(scanPackages = {"net.cocotea.cyreneadmin"})
public class SolonStarter {
    private static final Logger logger = LoggerFactory.getLogger(SolonStarter.class);

    public static void main(String[] args) {
        SolonApp app = Solon.start(SolonStarter.class, args);
        AppContext context = app.context();

        AppSystemProp appSystemProp = context.getBean(AppSystemProp.class);
        logger.warn("强密码：{}, 权限缓存状态：{}", appSystemProp.getStrongPassword(), appSystemProp.getPermissionCache());

        GlobalConst.START_TIME = System.currentTimeMillis();
    }
}
