package com.yinhai.mids.business.mapper;

import com.yinhai.mids.business.entity.dto.StudyPageQuery;
import com.yinhai.mids.business.entity.po.StudyPO;
import com.yinhai.mids.business.entity.vo.StudyPageVO;
import com.yinhai.mids.common.module.mybatis.SuperMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhuhs
 * @date 2024/7/2 9:30
 */
public interface StudyMapper extends SuperMapper<StudyPO> {

    List<StudyPageVO> list(@Param("query") StudyPageQuery query, @Param("userId") String userId);

}