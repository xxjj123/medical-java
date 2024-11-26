package com.yinhai.mids.business.spine;

import com.dtflys.forest.annotation.JSONBody;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.annotation.Var;
import com.dtflys.forest.http.ForestResponse;

public interface SpineClient {
    @Post(url = "{url}", connectTimeout = 30000, readTimeout = 300000)
    ForestResponse<SpineResponse> getBoneInfo(@Var("url") String url,
                                              @JSONBody("input") String base64Data);
}
