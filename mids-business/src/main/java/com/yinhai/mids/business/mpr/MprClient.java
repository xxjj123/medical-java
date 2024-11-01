package com.yinhai.mids.business.mpr;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;

import java.io.File;

/**
 * @author zhuhs
 * @date 2024/6/28 17:37
 */
public interface MprClient {
    @Post(url = "{url}", connectTimeout = 30000, readTimeout = 300000)
    ForestResponse<MprResponse> register(@Var("url") String url,
                                         @DataFile(value = "file", fileName = "dicom.zip") File file,
                                         @Body RegisterParam other);

    @Get(url = "{url}")
    @LogEnabled(value = false)
    ForestResponse<MprResponse> health(@Var("url") String url);
}
