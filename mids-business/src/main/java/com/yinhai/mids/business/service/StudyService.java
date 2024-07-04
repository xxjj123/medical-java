package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.dto.StudyPageQuery;
import com.yinhai.mids.business.entity.vo.StudyPageVO;
import com.yinhai.mids.common.core.PageRequest;
import com.yinhai.ta404.core.restservice.resultbean.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @author zhuhs
 * @date 2024/7/2 9:16
 */
public interface StudyService {

    void uploadDicom(MultipartFile dicom) throws IOException;

    Page<StudyPageVO> pageStudies(StudyPageQuery studyPageQuery, PageRequest pageRequest);

    void addFavorite(String studyId);

    void removeFavorite(String studyId);

    void deleteStudy(String studyId);

    void deleteSeries(String seriesId);
}
