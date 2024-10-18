package com.yinhai.mids.business.service.impl;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.mpr.MprClient;
import com.yinhai.mids.business.mpr.MprProperties;
import com.yinhai.mids.business.service.MprService;
import com.yinhai.mids.business.service.TaskLockService;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.storage.ta.core.FSManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zhuhs
 * @date 2024/7/18 15:25
 */
@Service
@TaTransactional
public class MprServiceImpl implements MprService {

    private static final Log log = LogFactory.get();

    @Resource
    private MprClient mprClient;

    @Resource
    private MprProperties mprProperties;

    @Resource
    private ITaFSManager<FSManager> fsManager;

    @Resource
    private TaskLockService taskLockService;

    @Override
    @SuppressWarnings("unchecked")
    public void doMprAnalyse(String seriesId) {
        // SeriesPO seriesPO = seriesMapper.selectById(seriesId);
        // if (seriesPO == null) {
        //     log.error("序列不存在", seriesId);
        //     throw new AppException("序列不存在");
        // }
        // boolean waitCompute = StrUtil.equals(seriesPO.getMprStatus(), ComputeStatus.WAIT_COMPUTE);
        // boolean computeTimeout = StrUtil.equals(seriesPO.getMprStatus(), ComputeStatus.IN_COMPUTE)
        //                          && seriesPO.getMprStartTime().before(DateUtil.offsetMinute(DbClock.now(), -5));
        // if (!(waitCompute || computeTimeout)) {
        //     return;
        // }
        //
        // ForestResponse<MprResponse> connectResponse = mprClient.health(mprProperties.getHealthUrl());
        // if (connectResponse.isError()) {
        //     log.error("连接MPR服务失败，请检查网络配置或MPR服务是否正常");
        //     return;
        // }
        // if (connectResponse.getResult().getCode() == 2) {
        //     log.error("MPR服务繁忙");
        //     return;
        // }
        //
        // LambdaQueryWrapper<InstancePO> queryWrapper = Wrappers.<InstancePO>lambdaQuery()
        //         .select(InstancePO::getAccessPath, InstancePO::getSopInstanceUid)
        //         .eq(InstancePO::getSeriesId, seriesId);
        // List<InstancePO> instancePOList = instanceMapper.selectList(queryWrapper);
        // if (CollUtil.isEmpty(instancePOList)) {
        //     log.error("序列{}对应实例不存在", seriesId);
        //     setErrorMprStatus(seriesId, null, StrUtil.format("序列{}对应实例不存在", seriesId));
        //     return;
        // }
        // RegisterParam registerParam = new RegisterParam();
        // registerParam.setSeriesId(seriesPO.getId());
        // registerParam.setCallbackUrl(mprProperties.getPushCallbackUrl());
        // MprResponse response = null;
        // try (InputStream inputStream = readDicomFromFSAndZip(instancePOList)) {
        //     ForestResponse<MprResponse> resp = mprClient.register(mprProperties.getRegisterUrl(), inputStream, registerParam);
        //     if (resp.isError()) {
        //         log.error("连接MPR服务失败，请检查网络配置或MPR服务是否正常");
        //         return;
        //     }
        //     response = resp.getResult();
        //     if (response.getCode() == 4) {
        //         log.error("{} 正在MPR", seriesId);
        //         return;
        //     }
        //     if (response.getCode() == 5) {
        //         log.error("MPR服务繁忙");
        //         return;
        //     }
        //     if (response.getCode() == 1) {
        //         seriesMapper.update(new SeriesPO(), Wrappers.<SeriesPO>lambdaUpdate()
        //                 .eq(SeriesPO::getId, seriesId)
        //                 .set(SeriesPO::getMprStatus, ComputeStatus.IN_COMPUTE)
        //                 .set(SeriesPO::getMprStartTime, DbClock.now())
        //                 .set(SeriesPO::getMprResponse, JsonKit.toJsonString(response)).
        //                 set(SeriesPO::getMprErrorMessage, null));
        //
        //     } else {
        //         setErrorMprStatus(seriesId, response, "申请Mpr分析失败");
        //     }
        // } catch (Exception e) {
        //     log.error(e);
        //     String errorMsg;
        //     if (e instanceof AppException) {
        //         errorMsg = ((AppException) e).getErrorMessage();
        //     } else {
        //         errorMsg = ExceptionUtil.getRootCauseMessage(e);
        //     }
        //     setErrorMprStatus(seriesId, response, errorMsg);
        // }

    }

    @Async
    @Override
    public void lockedAsyncDoMprAnalyse(String seriesId) {
    }
}
