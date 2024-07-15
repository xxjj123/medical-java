package com.yinhai.mids.business.analysis;

import com.dtflys.forest.annotation.*;

import java.io.InputStream;

/**
 * @author zhuhs
 * @date 2024/6/28 17:37
 */
public interface KeyaClient {

    @Post(url = "http://183.247.165.2:2480/connector/api/common/from/register/pacs", dataType = "json")
    KeyaResponse register(@Var("url") String url,
                          @DataFile(value = "files", fileName = "dicom") InputStream in, @Body RegisterParam other);

    @Get(url = "http://183.247.165.2:2480/connector/api/common/result", dataType = "json", readTimeout = 30000)
    KeyaResponse result(@Var("url") String url, @Query("applyId") String applyId);

}
