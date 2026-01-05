package net.cocotea.cyreneadmin.config;

import net.cocotea.cyreneadmin.properties.AppSystemProp;
import org.noear.redisx.RedisClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class AppConfig {

    @Value("${myapp.excludes}")
    private String excludes;

    @Value("${myapp.strong-password}")
    private String strongPassword;

    @Value("${myapp.password}")
    private String password;

    @Value("${myapp.password-salt}")
    private String passwordSalt;

    @Value("${myapp.permission-cache}")
    private Boolean permissionCache;

    @Value("${myapp.save-log}")
    private Boolean saveLog;

    @Value("${myapp.rd1.server}")
    private String rdServer;

    @Value("${myapp.rd1.password}")
    private String rdPassword;

    @Value("${myapp.rd1.db}")
    private String rdDb;

    @Value("${myapp.file.avatar}")
    private String avatar;

    @Value("${myapp.file.support-filetype}")
    private String supportFiletype;

    @Bean
    public AppSystemProp appSystemProp() {
        return new AppSystemProp()
                .setExcludes(excludes)
                .setStrongPassword(strongPassword)
                .setPassword(password)
                .setPasswordSalt(passwordSalt)
                .setPermissionCache(permissionCache)
                .setSaveLog(saveLog)
                .setAvatarPath(avatar)
                .setSupportFiletype(supportFiletype);
    }

    @Bean
    public RedisClient redisClient() {
        Properties props = new Properties();
        props.put("server", rdServer);
        props.put("password",rdPassword);
        props.put("db",rdDb);
       return new RedisClient(props);
    }

}
