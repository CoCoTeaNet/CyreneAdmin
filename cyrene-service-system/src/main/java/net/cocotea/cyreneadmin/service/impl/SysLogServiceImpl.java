package net.cocotea.cyreneadmin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.thread.ThreadUtil;
import lombok.RequiredArgsConstructor;
import net.cocotea.cyreneadmin.model.dto.SysLogAddDTO;
import net.cocotea.cyreneadmin.model.dto.SysLogPageDTO;
import net.cocotea.cyreneadmin.model.dto.SysLogUpdateDTO;
import net.cocotea.cyreneadmin.model.po.SysLog;
import net.cocotea.cyreneadmin.model.vo.SysLogVO;
import net.cocotea.cyreneadmin.properties.AppSystemProp;
import net.cocotea.cyreneadmin.service.SysLogService;
import net.cocotea.cyreneadmin.enums.LogStatusEnum;
import net.cocotea.cyreneadmin.enums.LogTypeEnum;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.BusinessException;
import net.cocotea.cyreneadmin.util.LoginUtils;
import org.sagacity.sqltoy.dao.LightDao;
import org.sagacity.sqltoy.model.Page;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class SysLogServiceImpl implements SysLogService {

    private final AppSystemProp appSystemProp;
    private final LightDao lightDao;

    @Override
    public boolean add(SysLogAddDTO sysLogAddDTO) throws BusinessException {
        SysLog sysOperationLog = BeanUtil.toBean(sysLogAddDTO, SysLog.class);
        Object save = lightDao.save(sysOperationLog);
        return save != null;
    }

    @Override
    public boolean deleteBatch(List<BigInteger> idList) throws BusinessException {
        idList.forEach(this::delete);
        return !idList.isEmpty();
    }

    @Override
    public boolean update(SysLogUpdateDTO param) throws BusinessException {
        return false;
    }

    @Override
    public boolean delete(BigInteger id) {
        return lightDao.deleteByIds(SysLog.class, id) > 0;
    }

    @Override
    public ApiPage<SysLogVO> listByPage(SysLogPageDTO pageDTO) throws BusinessException {
        String operator = pageDTO.getSysLog().getOperator();

        Map<String, Object> sysLogMap = BeanUtil.beanToMap(pageDTO.getSysLog());
        sysLogMap.put("operator", operator);

        Page<SysLogVO> logVOPage = ApiPage.create(pageDTO);
        Page<SysLogVO> page = lightDao.findPage(logVOPage, "sys_log_JOIN_findList", sysLogMap, SysLogVO.class);

        return ApiPage.rest(page);
    }

    @Override
    public void saveByLogType(Integer logType, String realIp, String methodName) {
        BigInteger loginId = LoginUtils.loginId();
        ThreadUtil.execAsync(() -> {
            if (appSystemProp.getSaveLog()) {
                SysLogAddDTO sysLogAddDTO = new SysLogAddDTO();
                sysLogAddDTO.setIpAddress(realIp);
                sysLogAddDTO.setLogType(logType);
                sysLogAddDTO.setRequestWay(methodName);
                sysLogAddDTO.setOperator(loginId);
                sysLogAddDTO.setLogStatus(LogStatusEnum.SUCCESS.getCode());
                try {
                    add(sysLogAddDTO);
                } catch (BusinessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void saveErrorLog(String realIp, String methodName) {
        if (StpUtil.isLogin() && appSystemProp.getSaveLog()) {
            SysLogAddDTO sysLogAddDTO = new SysLogAddDTO();
            sysLogAddDTO.setIpAddress(realIp);
            sysLogAddDTO.setLogType(LogTypeEnum.OPERATION.getCode());
            sysLogAddDTO.setRequestWay(methodName);
            sysLogAddDTO.setOperator(LoginUtils.loginId());
            sysLogAddDTO.setLogStatus(LogStatusEnum.ERROR.getCode());
            try {
                add(sysLogAddDTO);
            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
