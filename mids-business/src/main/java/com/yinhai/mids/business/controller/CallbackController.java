package com.yinhai.mids.business.controller;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.service.ComputeSeriesService;
import com.yinhai.mids.business.service.DiagnoseService;
import com.yinhai.ta404.core.restservice.annotation.RestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhuhs
 * @date 2024/7/12 14:13
 */
@Validated
@Tag(name = "分析回调")
@RestService("callback")
public class CallbackController {

    private static final Log log = LogFactory.get();

    @Resource
    private ComputeSeriesService computeSeriesService;

    @Resource
    private DiagnoseService diagnoseService;

    @Operation(summary = "AI结果推送")
    @PostMapping("computePush")
    public void computePush(@RequestParam Map<String, Object> pushParamMap) {
        computeSeriesService.onComputePush(pushParamMap);
    }

    @Operation(summary = "3D解析结果推送")
    @PostMapping("analysePush")
    public void analysePush(@RequestPart @NotNull(message = "回传文件不能为空") MultipartFile file,
                            @RequestParam @NotBlank(message = "申请ID不能为空") String applyId) {

    }


    @Operation(summary = "MPR解析结果推送")
    @PostMapping("mprAnalysePush")
    public ResponseEntity<Map<String, Object>> uploadFile (
            @RequestParam(value="vti_file",required = false) MultipartFile vti_file,
            @RequestParam(value="glb_file",required = false) MultipartFile glb_file,
            @RequestParam(value="seriesId" ) @NotNull(message = "计算序列不能为空")  String seriesId,
            @RequestParam(value="code") @NotNull(message = "code不能为空")  String code,
            @RequestParam(value="message" )String message) throws IOException {


        Map<String, Object> response = new HashMap<>();

        response.put("redirectUrl", null);

        try {
            if (vti_file == null || vti_file.isEmpty()) {
                response.put("code", 300);  // 操作失败
                response.put("message", "vti文件为空");
                response.put("serviceSuccess", false);
                return ResponseEntity.ok(response);
            }
            if (glb_file == null || glb_file.isEmpty()) {
                response.put("code", 300);  // 操作失败
                response.put("message", "glb文件为空");
                response.put("serviceSuccess", false);
                return ResponseEntity.ok(response);
            }

            // 在此处处理文件，例如保存或其他操作
            // ...
            diagnoseService.onMprPush(vti_file,seriesId,code,message);

            response.put("code", 200);  // 操作成功
            response.put("message", "文件处理成功");
            response.put("serviceSuccess", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("code", 500);  // 系统错误
            response.put("message", "系统错误");
            response.put("serviceSuccess", false);
            return ResponseEntity.status(500).body(response);
        }

    }
}
