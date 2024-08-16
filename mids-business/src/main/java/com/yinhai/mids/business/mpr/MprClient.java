package com.yinhai.mids.business.mpr;

import com.dtflys.forest.annotation.*;

import java.io.InputStream;

/**
 * @author zhuhs
 * @date 2024/6/28 17:37
 */
public interface MprClient {
    @Post(url = "{url}", dataType = "json")
    MprResponse register(@Var("url") String url,
                         @DataFile(value = "file", fileName = "dicom.zip") InputStream in, @Body RegisterParam other);

    @Get(url = "{url}", dataType = "json", readTimeout = 30000)
    MprResponse result(@Var("url") String url, @Query("applyId") String applyId);

}
