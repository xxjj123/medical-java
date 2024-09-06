package com.yinhai.mids.business.mpr;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;

import java.io.InputStream;

/**
 * @author zhuhs
 * @date 2024/6/28 17:37
 */
public interface MprClient {
    @Post(url = "{url}")
    ForestResponse<MprResponse> register(@Var("url") String url,
                         @DataFile(value = "file", fileName = "dicom.zip") InputStream in, @Body RegisterParam other);

    @Post(url = "{url}")
    ForestResponse<MprResponse> testConnect(@Var("url") String url);
}
