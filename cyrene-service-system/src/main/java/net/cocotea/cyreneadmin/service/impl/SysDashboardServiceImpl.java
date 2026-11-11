package net.cocotea.cyreneadmin.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.system.HostInfo;
import cn.hutool.system.OsInfo;
import cn.hutool.system.SystemUtil;
import cn.hutool.system.oshi.CpuInfo;
import cn.hutool.system.oshi.OshiUtil;
import lombok.RequiredArgsConstructor;
import net.cocotea.cyreneadmin.model.po.SysMenu;
import net.cocotea.cyreneadmin.model.po.SysRole;
import net.cocotea.cyreneadmin.model.po.SysUser;
import net.cocotea.cyreneadmin.model.vo.SysOverviewVO;
import net.cocotea.cyreneadmin.model.vo.SystemInfoVO;
import net.cocotea.cyreneadmin.service.SysDashboardService;
import net.cocotea.cyreneadmin.constant.GlobalConst;
import net.cocotea.cyreneadmin.constant.RedisKeyConst;
import net.cocotea.cyreneadmin.enums.IsEnum;
import net.cocotea.cyreneadmin.enums.MenuTypeEnum;
import net.cocotea.cyreneadmin.service.RedisService;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.lambda.LambdaQuery;
import oshi.hardware.GlobalMemory;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author CoCoTea
 */
@RequiredArgsConstructor
public class SysDashboardServiceImpl implements SysDashboardService {
    
    private final LightDao lightDao;
    private final RedisService redisService;

    @Override
    public List<SysOverviewVO> getCount() {
        List<SysOverviewVO> sysOverviewList = new ArrayList<>(4);

        long countUser = LambdaQuery.create(SysUser.class).bind(lightDao)
                .eq(true, SysUser::getIsDeleted, IsEnum.N.getCode())
                .count();
        sysOverviewList.add(new SysOverviewVO().setTitle("用户数量").setCount(countUser));

        long countMenu = LambdaQuery.create(SysMenu.class).bind(lightDao)
                .eq(true, SysMenu::getIsDeleted, IsEnum.N.getCode())
                .eq(true, SysMenu::getIsMenu, IsEnum.Y.getCode())
                .eq(true, SysMenu::getMenuType, MenuTypeEnum.MENU.getCode())
                .count();
        sysOverviewList.add(new SysOverviewVO().setTitle("菜单数量").setCount(countMenu));

        long countRole = LambdaQuery.create(SysRole.class).bind(lightDao)
                .eq(true, SysRole::getIsDeleted, IsEnum.N.getCode())
                .count();
        sysOverviewList.add(new SysOverviewVO().setTitle("角色数量").setCount(countRole));

        long countOnline = redisService.keys(
                String.format(RedisKeyConst.ONLINE_USER, "*")
        ).size();
        sysOverviewList.add(new SysOverviewVO().setTitle("在线用户").setCount(countOnline));
        return sysOverviewList;
    }

    @Override
    public SystemInfoVO getSystemInfo() {
        SystemInfoVO systemInfoVO = new SystemInfoVO();
        // 服务器信息
        OsInfo osInfo = SystemUtil.getOsInfo();
        HostInfo hostInfo = SystemUtil.getHostInfo();
        systemInfoVO.setOs(osInfo.getName());
        systemInfoVO.setServerName(hostInfo.getName());
        systemInfoVO.setServerIp(hostInfo.getAddress());
        systemInfoVO.setServerArchitecture(osInfo.getArch());
        // java信息
        systemInfoVO.setJavaName(SystemUtil.getJvmInfo().getName());
        systemInfoVO.setJavaVersion(SystemUtil.getJavaInfo().getVersion());
        systemInfoVO.setJavaPath(SystemUtil.getJavaRuntimeInfo().getHomeDir());
        // 服务运行信息
        systemInfoVO.setProjectPath(System.getProperty("user.dir"));
        Duration between = LocalDateTimeUtil.between(
                LocalDateTimeUtil.ofUTC(GlobalConst.START_TIME),
                LocalDateTimeUtil.ofUTC(System.currentTimeMillis())
        );
        systemInfoVO.setRunningTime(between.getSeconds());
        // CPU信息
        CpuInfo cpuInfo = OshiUtil.getCpuInfo();
        systemInfoVO.setCpuSystemUsed(cpuInfo.getSys());
        systemInfoVO.setCpuUserUsed(cpuInfo.getUsed());
        systemInfoVO.setCpuCount(cpuInfo.getCpuNum());
        systemInfoVO.setCpuFree(cpuInfo.getFree());
        // 内存信息
        GlobalMemory memory = OshiUtil.getMemory();
        systemInfoVO.setMemoryAvailableSize(memory.getAvailable());
        systemInfoVO.setMemoryTotalSize(memory.getTotal());
        // 磁盘信息
        File file = new File(SystemUtil.get(SystemUtil.FILE_SEPARATOR));
        systemInfoVO.setDiskTotalSize(file.getTotalSpace());
        systemInfoVO.setDiskFreeSize(file.getFreeSpace());
        systemInfoVO.setDiskPath(SystemUtil.get(SystemUtil.FILE_SEPARATOR));
        systemInfoVO.setDiskSeparator(SystemUtil.get(SystemUtil.FILE_SEPARATOR));
        return systemInfoVO;
    }
}
