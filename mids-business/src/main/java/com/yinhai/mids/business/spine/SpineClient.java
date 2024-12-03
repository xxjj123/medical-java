package com.yinhai.mids.business.spine;

import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.LogEnabled;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.annotation.Var;
import com.dtflys.forest.http.ForestResponse;

public interface SpineClient {

    @Post(url = "{url}", connectTimeout = 30000, readTimeout = 300000)
    @LogEnabled(value = false)
    ForestResponse<SpineResponse> getBoneInfo(@Var("url") String url, @JSONBody("input") String base64Data);

    @Post(url = "{url}")
    @LogEnabled(value = false)
    ForestResponse<SpineResponse> testConnect(@Var("url") String url);
}
