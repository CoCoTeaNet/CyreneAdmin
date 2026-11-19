package net.cocotea.cyreneadmin.util;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import net.cocotea.cyreneadmin.model.BusinessException;

import java.io.*;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传工具类
 *
 * @author jwss
 * @date 2022-4-9 23:34:04
 */
public class FileUploadUtils {

    public static void filter(String originalFilename, String supportFiletype) throws BusinessException {
        if (originalFilename != null) {
            String[] split = originalFilename.split("\\.");
            String fileType = split[split.length - 1];
            if (StrUtil.isBlank(fileType)) {
                throw new BusinessException("未知文件格式");
            } else {
                boolean supportFlag = Arrays.stream(supportFiletype.split(StrPool.COMMA))
                        .toList()
                        .contains(fileType);
                Assert.isTrue(supportFlag, () -> new BusinessException("该文件格式不支持上传"));
            }
        } else {
            throw new BusinessException("文件名为空");
        }
    }

    /**
     * 验证头像文件
     * <p>
     * 仅支持jpg、jpeg、png、gif格式
     * @param file 文件
     * @throws BusinessException 验证失败
     */
    public static void validAvatar(File file) throws BusinessException, IOException {
        Assert.isFalse(file == null, () -> new BusinessException("空文件对象"));
        Assert.isTrue(file.exists(), () -> new BusinessException("文件不存在"));
        String type;
        try (BufferedInputStream inputStream = FileUtil.getInputStream(file)) {
            type = FileTypeUtil.getType(inputStream);
        }
        Assert.isFalse(type == null, () -> {
            FileUtil.del(file);
            return new BusinessException("未知文件格式");
        });
        Assert.isTrue(List.of("jpg", "jpeg", "png", "gif").contains(type), () -> {
            FileUtil.del(file);
            return new BusinessException("仅支持jpg、jpeg、png、gif格式");
        });
    }

}
