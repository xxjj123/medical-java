package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.dto.AlgorithmParam;
import com.yinhai.mids.business.entity.dto.StudyPageQuery;
import com.yinhai.mids.business.entity.vo.StudyPageVO;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.ta404.core.restservice.resultbean.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/7/2 9:16
 */
public interface StudyService {

    /**
     * 上传DICOM文件
     *
     * @param dicom              DICOM文件
     * @param algorithmParamList 算法配置参数
     * @throws IOException IOException
     * @author zhuhs 2024/07/11 16:36
     */
    void uploadDicom(MultipartFile dicom, List<AlgorithmParam> algorithmParamList) throws IOException;

    /**
     * 分页查询检查
     *
     * @param studyPageQuery 查询参数
     * @param pageRequest    分页参数
     * @return {@link Page }<{@link StudyPageVO }>
     * @author zhuhs 2024/07/11 16:36
     */
    Page<StudyPageVO> pageStudies(StudyPageQuery studyPageQuery, PageRequest pageRequest);

    /**
     * 添加收藏
     *
     * @param studyId 检查ID
     * @author zhuhs 2024/07/11 16:37
     */
    void addFavorite(String studyId);

    /**
     * 取消收藏
     *
     * @param studyId 检查ID
     * @author zhuhs 2024/07/11 16:37
     */
    void removeFavorite(String studyId);

    /**
     * 删除检查
     *
     * @param studyId 检查ID
     * @author zhuhs 2024/07/11 16:37
     */
    void deleteStudy(String studyId);

    /**
     * 删除序列
     *
     * @param computeSeriesId 计算序列ID
     * @author zhuhs 2024/07/11 16:37
     */
    void deleteComputeSeries(String computeSeriesId);
}
