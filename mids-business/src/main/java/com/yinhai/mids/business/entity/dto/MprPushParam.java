package com.yinhai.mids.business.entity.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author zhuhs
 * @date 2024/10/22
 */
@Data
public class MprPushParam {

    @JsonIgnore
    private MultipartFile file;

    private String type;

    private String seriesId;

    private String applyId;

    private String code;

    private String message;
}
