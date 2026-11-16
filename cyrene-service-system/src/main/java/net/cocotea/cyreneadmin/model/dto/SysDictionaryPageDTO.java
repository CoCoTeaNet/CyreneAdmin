package net.cocotea.cyreneadmin.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import net.cocotea.cyreneadmin.model.ApiPageDTO;
import net.cocotea.cyreneadmin.model.vo.SysDictionaryVO;

import java.io.Serial;
import java.io.Serializable;

/**
 * 字典分页参数
 *
 * @author CoCoTea
 * @date 2022-3-22
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class SysDictionaryPageDTO extends ApiPageDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 8818887425379309640L;

    /**
     * 查询条件
     */
    private SysDictionaryVO sysDictionary;
}
