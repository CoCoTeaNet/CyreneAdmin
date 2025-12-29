package net.cocotea.cyreneadmin.filter;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import net.cocotea.cyreneadmin.model.ApiResult;
import org.noear.solon.core.aspect.Interceptor;
import org.noear.solon.core.aspect.Invocation;
import org.noear.solon.core.wrap.ParamWrap;
import org.noear.solon.validation.annotation.Validated;

/**
 * 参数验证拦截器
 *
 * @author CoCoTea
 */
public class ParameterInterceptor implements Interceptor {
    @Override
    public Object doIntercept(Invocation inv) throws Throwable {
        for (int i = 0; i < inv.method().getParamWraps().length; i++) {
            ParamWrap paramWrap = inv.method().getParamWraps()[i];
            Validated annotation = paramWrap.getAnnotation(Validated.class);
            if (annotation != null) {
                Validator myValidator = inv.context().getBean(Validator.class);
                for (ConstraintViolation<Object> violation : myValidator.validate(inv.args()[i])) {
                    return ApiResult.error(violation.getMessage());
                }
            }
        }
        return inv.invoke();
    }
}
