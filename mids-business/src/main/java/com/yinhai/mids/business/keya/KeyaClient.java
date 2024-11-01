package com.yinhai.mids.business.keya;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;

import java.io.File;

/**
 * @author zhuhs
 * @date 2024/6/28 17:37
 */
public interface KeyaClient {

    @Post(url = "{url}", connectTimeout = 30000, readTimeout = 300000)
    ForestResponse<KeyaResponse> applyCompute(@Var("url") String url, @DataFile(value = "files") File file, @Body RegisterParam other);

    @Get(url = "{url}", connectTimeout = 30000, readTimeout = 60000)
    ForestResponse<KeyaResponse> queryComputeResult(@Var("url") String url, @Query("applyId") String applyId);

    @Post(url = "{url}")
    @LogEnabled(value = false)
    ForestResponse<KeyaResponse> testConnect(@Var("url") String url);
}
