package com.yinhai.mids.business.service;

import com.yinhai.mids.business.entity.vo.SingleImageInfoVO;
import com.yinhai.mids.business.entity.vo.SpineInfoVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface SingleImageService {
    void uploadDicom(MultipartFile dicom) throws IOException;

    SingleImageInfoVO queryInitInfo(String studyid);

    void downloadSlice(String studyId, HttpServletResponse response);

    SpineInfoVO getSpineInfo(MultipartFile dicom) throws IOException;

}
