package com.yinhai.mids.business.analysis.keya;

import com.dtflys.forest.annotation.*;

import java.io.InputStream;

/**
 * @author zhuhs
 * @date 2024/6/28 17:37
 */
public interface KeyaClient {

    @Post(url = "http://183.247.165.2:2480/connector/api/common/from/register/pacs", dataType = "json")
    KeyaResponse register(@DataFile(value = "files", fileName = "dicom") InputStream in, @Body RegisterBody other);

    @Get(url = "http://183.247.165.2:2480/connector/api/common/result", dataType = "json", readTimeout = 30000)
    KeyaResponse result(@Query("applyId") String applyId);

}
