package com.yinhai.mids.business.spine;

import com.dtflys.forest.annotation.*;
import com.dtflys.forest.http.ForestResponse;

import java.io.InputStream;

public interface SpineClient {

    @Post(url = "{url}", connectTimeout = 30000, readTimeout = 300000)
    @LogEnabled(value = false)
    ForestResponse<SpineResponse> getBoneInfo(@Var("url") String url, @DataFile(value = "file", fileName = "spine") InputStream inputStream);

    @Post(url = "{url}")
    @LogEnabled(value = false)
    ForestResponse<SpineResponse> testConnect(@Var("url") String url);
}
