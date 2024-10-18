package com.yinhai.mids.business.mapper;

import com.yinhai.mids.business.entity.dto.CaseStudyQuery;
import com.yinhai.mids.business.entity.po.CasePO;
import com.yinhai.mids.business.entity.vo.CaseSeriesVO;
import com.yinhai.mids.business.entity.vo.CaseStudyVO;
import com.yinhai.mids.common.module.mybatis.SuperMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author zhuhs
 * @date 2024/10/14
 */
public interface CaseMapper extends SuperMapper<CasePO> {

    List<CaseStudyVO> queryCaseStudies(@Param("query") CaseStudyQuery caseStudyQuery, @Param("userId") String userId);

    List<CaseSeriesVO> queryCaseSeries(@Param("studyInfoIds") List<String> studyInfoIds,
                                       @Param("computeType") Integer computeType,
                                       @Param("computeStatus") Integer computeStatus);
}