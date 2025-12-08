package net.cocotea.cyreneadmin.handler;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.annotation.Resource;
import net.cocotea.cyreneadmin.enums.IsEnum;
import net.cocotea.cyreneadmin.model.vo.SysMenuVO;
import net.cocotea.cyreneadmin.model.vo.SysRoleVO;
import net.cocotea.cyreneadmin.properties.AppSystemProp;
import net.cocotea.cyreneadmin.service.SysMenuService;
import net.cocotea.cyreneadmin.service.SysRoleService;
import net.cocotea.cyreneadmin.util.LoginUtils;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取用户权限集合
 *
 * @author CoCoTea
 * @version 2.0.0
 */
@Component
public class StpInterfaceInterceptor implements StpInterface {
    @Resource
    private SysMenuService sysMenuService;
    @Resource
    private SysRoleService sysRoleService;
    @Resource
    private AppSystemProp appSystemProp;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        StpUtil.checkLogin();
        BigInteger myLoginId = LoginUtils.parse(loginId);
        List<SysMenuVO> cachePermissionList = sysMenuService.getCachePermission(myLoginId);
        List<String> list;
        // 1关闭了缓存 2缓存失效了 3有缓存
        if (!appSystemProp.getPermissionCache()) {
            List<SysMenuVO> menuList = sysMenuService.listByUserId(IsEnum.N.getCode());
            list = new ArrayList<>(menuList.size());
            menuList.forEach(item -> list.add(item.getPermissionCode()));
        } else if (cachePermissionList == null) {
            List<SysMenuVO> permission = sysMenuService.cachePermission(myLoginId);
            list = new ArrayList<>(permission.size());
            permission.forEach(i -> list.add(i.getPermissionCode()));
        } else {
            list = new ArrayList<>(cachePermissionList.size());
            cachePermissionList.forEach(i -> list.add(i.getPermissionCode()));
        }
        return list;
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        StpUtil.checkLogin();
        List<SysRoleVO> roles = sysRoleService.loadByUserId(LoginUtils.parse(loginId));
        List<String> roleKeys = new ArrayList<>(roles.size());
        for (SysRoleVO role : roles) {
            roleKeys.add(role.getRoleKey());
        }
        return roleKeys;
    }
}
