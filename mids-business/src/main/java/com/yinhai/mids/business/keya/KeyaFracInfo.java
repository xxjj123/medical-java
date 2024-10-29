package com.yinhai.mids.business.keya;

import lombok.Data;

import java.util.List;

/**
 * @author zhuhs
 * @date 2024/10/28
 */
@Data
public class KeyaFracInfo {

    private String ribSide;

    private String ribType;

    private Integer ribNum;

    private String fracClass;

    private List<String> fracBBox;
}
