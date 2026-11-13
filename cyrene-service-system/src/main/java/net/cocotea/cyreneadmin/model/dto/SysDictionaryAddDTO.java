package net.cocotea.cyreneadmin.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigInteger;

/**
 * @author CoCoTea
 * @version 2.0.0
 */
@Data
@Accessors(chain = true)
public class SysDictionaryAddDTO implements Serializable {
	@Serial
	private static final long serialVersionUID = -1767974905556894292L;

	/**
	 * 父节点主键ID
	 */
	private BigInteger parentId;

	/**
	 * 字典名称
	 */
	@NotBlank(message = "字典名称不能为空")
	private String dictionaryName;

	/**
	 * 字典值;分类行为查找键,子项为枚举码
	 */
	private String dictionaryValue;

	/**
	 * 英文名称
	 */
	private String nameEn;

	/**
	 * 标签颜色;success/warning/danger/info/primary
	 */
	private String tagType;

	/**
	 * 备注
	 */
	private String remark;

	/**
	 * 序号
	 */
	private Integer sort;

	/**
	 * 启用状态;0关闭 1启用
	 */
	private Integer enableStatus;
}
