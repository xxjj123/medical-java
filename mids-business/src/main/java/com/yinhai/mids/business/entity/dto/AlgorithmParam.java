package com.yinhai.mids.business.entity.dto;

import lombok.Data;

import java.util.List;

/**
 * @author zhuhs
 * @date 2024/7/10 17:29
 */
@Data
public class AlgorithmParam {

    private String seriesInstanceUid;

    private List<String> algorithmTypeList;

}
