package net.cocotea.cyreneadmin.service;

import net.cocotea.cyreneadmin.enums.LogTypeEnum;
import net.cocotea.cyreneadmin.model.dto.SysLogAddDTO;
import net.cocotea.cyreneadmin.model.dto.SysLogPageDTO;
import net.cocotea.cyreneadmin.model.dto.SysLogUpdateDTO;
import net.cocotea.cyreneadmin.model.vo.SysLogVO;
import net.cocotea.cyreneadmin.model.ApiPage;
import net.cocotea.cyreneadmin.model.BusinessException;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
public interface SysLogService extends BaseService<ApiPage<SysLogVO>, SysLogPageDTO, SysLogAddDTO, SysLogUpdateDTO> {
    /**
     * 通过日志类型保存
     *
     * @param logType {@link LogTypeEnum}
     * @param request {@link HttpServletRequest}
     * @throws BusinessException 异常抛出
     */
    void saveByLogType(Integer logType, String realIp, String methodName) throws BusinessException;

    /**
     * 错误日志保存
     *
     * @param request {@link HttpServletRequest}
     */
    void saveErrorLog(String realIp, String methodName);
}
