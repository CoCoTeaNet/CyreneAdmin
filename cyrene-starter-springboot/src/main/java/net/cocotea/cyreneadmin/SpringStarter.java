package net.cocotea.cyreneadmin;

import net.cocotea.cyreneadmin.constant.GlobalConst;
import net.cocotea.cyreneadmin.properties.AppSystemProp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringStarter {

    private static final Logger logger = LoggerFactory.getLogger(SpringStarter.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(SpringStarter.class, args);
        AppSystemProp appSystemProp = (AppSystemProp) context.getBean("appSystemProp");
        logger.info("强密码：{}, 权限缓存状态：{}", appSystemProp.getStrongPassword(), appSystemProp.getPermissionCache());
        GlobalConst.START_TIME = System.currentTimeMillis();
    }

}
