package com.yinhai.mids.business.mapper;

import com.yinhai.ta404.module.mybatis.mapper.Ta404SupportMapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author zhuhs
 * @date 2024/10/31
 */
public interface SeqMapper extends Ta404SupportMapper {

    int addSeq(@Param("seqName") String seqName);
}
