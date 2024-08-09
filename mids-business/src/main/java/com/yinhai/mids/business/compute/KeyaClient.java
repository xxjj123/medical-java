package com.yinhai.mids.business.compute;

import com.dtflys.forest.annotation.*;

import java.io.InputStream;

/**
 * @author zhuhs
 * @date 2024/6/28 17:37
 */
public interface KeyaClient {

    @Post(url = "{url}", dataType = "json")
    KeyaResponse register(@Var("url") String url,
                          @DataFile(value = "files", fileName = "dicom") InputStream in, @Body RegisterParam other);

    @Get(url = "{url}", dataType = "json", readTimeout = 30000)
    KeyaResponse result(@Var("url") String url, @Query("applyId") String applyId);

}
