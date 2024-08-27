package com.yinhai.mids.common.module.validate;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

/**
 * 设置参数允许的传值，支持String和Integer类型。String类型时支持多值拼接形式
 *
 * @author zhuhs
 * @date 2024/8/27
 */
@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = AllowedValuesValidator.class)
public @interface AllowedValues {

    String message() default "码值不合法";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    /**
     * 允许的值，优先级高于字典{@link #dictType()}
     *
     * @return {@link String[] }
     * @author zhuhs 2024/08/27
     */
    String[] allowed() default {};

    /**
     * 字典类型
     *
     * @return {@link String }
     * @author zhuhs 2024/08/27
     */
    String dictType() default "";

    /**
     * 是否是分隔符拼接形式
     *
     * @return boolean
     * @author zhuhs 2024/08/27
     */
    boolean delimited() default false;

    /**
     * 分隔符
     *
     * @return {@link String }
     * @author zhuhs 2024/08/27
     */
    String delimiter() default ",";
}
