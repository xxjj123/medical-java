package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.dto.CaseStudyQuery;
import com.yinhai.mids.business.entity.vo.CaseStudyVO;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.ta404.core.restservice.resultbean.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author zhuhs
 * @date 2024/10/14
 */
public interface CaseService {

    /**
     * 创建案例
     */
    void createCase(MultipartFile caseFile) throws IOException;

    /**
     * 分页查询案例检查
     */
    Page<CaseStudyVO> pageCaseStudies(CaseStudyQuery caseStudyQuery, PageRequest pageRequest);

    /**
     * 添加收藏
     */
    void addFavorite(String studyInfoId);

    /**
     * 取消收藏
     */
    void removeFavorite(String studyInfoId);

    /**
     * 删除检查
     */
    void deleteCaseStudy(String studyInfoId);

    /**
     * 删除序列
     */
    void deleteCaseSeries(String computeTaskId);

    /**
     * 重新分析检查
     */
    void reComputeStudy(String studyInfoId);

    /**
     * 重新分析序列
     */
    void reComputeSeries(String computeTaskId);
}
