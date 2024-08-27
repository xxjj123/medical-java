package com.yinhai.mids.common.module.validate;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.yinhai.ta404.module.dict.entity.TaDictPo;
import com.yinhai.ta404.module.dict.util.DictUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/8/27
 */
public class AllowedValuesValidator implements ConstraintValidator<AllowedValues, Object> {

    private String[] allowed;

    private boolean delimited;

    private String delimiter;

    @Override
    public void initialize(AllowedValues anno) {
        if (ArrayUtil.isNotEmpty(anno.allowed())) {
            this.allowed = anno.allowed();
        } else if (StrUtil.isNotBlank(anno.dictType())) {
            List<TaDictPo> codeList = DictUtils.getCodeList(anno.dictType(), "0");
            if (CollUtil.isNotEmpty(codeList)) {
                allowed = codeList.stream().map(TaDictPo::getValue).toArray(String[]::new);
            }
        } else {
            allowed = new String[]{};
        }
        this.delimited = anno.delimited();
        this.delimiter = anno.delimiter();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        if (value instanceof String) {
            String param = (String) value;
            if (StrUtil.isBlank(param)) {
                return true;
            }
            if (delimited) {
                List<String> split = StrUtil.split(param, delimiter, false, false);
                for (String s : split) {
                    if (!isAllowed(s)) {
                        return false;
                    }
                }
                return true;
            } else {
                return isAllowed(param);
            }
        }
        if (value instanceof Integer) {
            return isAllowed(String.valueOf(value));
        }
        return true;
    }

    private boolean isAllowed(String code) {
        for (String s : allowed) {
            if (StrUtil.equals(code, s)) {
                return true;
            }
        }
        return false;
    }
}
