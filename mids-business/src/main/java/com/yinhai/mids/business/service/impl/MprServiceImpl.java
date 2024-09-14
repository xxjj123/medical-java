package com.yinhai.mids.business.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dtflys.forest.http.ForestResponse;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.constant.TaskType;
import com.yinhai.mids.business.entity.po.InstancePO;
import com.yinhai.mids.business.entity.po.SeriesPO;
import com.yinhai.mids.business.mapper.InstanceMapper;
import com.yinhai.mids.business.mapper.SeriesMapper;
import com.yinhai.mids.business.mpr.MprClient;
import com.yinhai.mids.business.mpr.MprProperties;
import com.yinhai.mids.business.mpr.MprResponse;
import com.yinhai.mids.business.mpr.RegisterParam;
import com.yinhai.mids.business.service.MprService;
import com.yinhai.mids.business.service.TaskLockService;
import com.yinhai.mids.common.util.DbKit;
import com.yinhai.mids.common.util.JsonKit;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author zhuhs
 * @date 2024/7/18 15:25
 */
@Service
@TaTransactional
public class MprServiceImpl implements MprService {

    private static final Log log = LogFactory.get();

    @Resource
    private SeriesMapper seriesMapper;

    @Resource
    private InstanceMapper instanceMapper;

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
        SeriesPO seriesPO = seriesMapper.selectById(seriesId);
        if (seriesPO == null) {
            log.error("序列不存在", seriesId);
            throw new AppException("序列不存在");
        }
        boolean waitCompute = StrUtil.equals(seriesPO.getMprStatus(), ComputeStatus.WAIT_COMPUTE);
        boolean computeTimeout = StrUtil.equals(seriesPO.getMprStatus(), ComputeStatus.IN_COMPUTE)
                                 && seriesPO.getMprStartTime().before(DateUtil.offsetMinute(DbKit.now(), -5));
        if (!(waitCompute || computeTimeout)) {
            return;
        }

        ForestResponse<MprResponse> connectResponse = mprClient.testConnect(mprProperties.getRegisterUrl());
        if (connectResponse.isError()) {
            log.error("连接MPR服务失败，请检查网络配置或MPR服务是否正常");
            return;
        }

        LambdaQueryWrapper<InstancePO> queryWrapper = Wrappers.<InstancePO>lambdaQuery()
                .select(InstancePO::getAccessPath, InstancePO::getSopInstanceUid)
                .eq(InstancePO::getSeriesId, seriesId);
        List<InstancePO> instancePOList = instanceMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(instancePOList)) {
            log.error("序列{}对应实例不存在", seriesId);
            setErrorMprStatus(seriesId, null, StrUtil.format("序列{}对应实例不存在", seriesId));
            return;
        }
        RegisterParam registerParam = new RegisterParam();
        registerParam.setSeriesId(seriesPO.getId());
        registerParam.setCallbackUrl(mprProperties.getPushCallbackUrl());
        MprResponse response = null;
        try (InputStream inputStream = readDicomFromFSAndZip(instancePOList)) {
            ForestResponse<MprResponse> resp = mprClient.register(mprProperties.getRegisterUrl(), inputStream, registerParam);
            if (resp.isError()) {
                log.error("连接MPR服务失败，请检查网络配置或MPR服务是否正常");
                return;
            }
            response = resp.getResult();
            if (response.getCode() == 1) {
                seriesMapper.update(new SeriesPO(), Wrappers.<SeriesPO>lambdaUpdate()
                        .eq(SeriesPO::getId, seriesId)
                        .set(SeriesPO::getMprStatus, ComputeStatus.IN_COMPUTE)
                        .set(SeriesPO::getMprStartTime, DbKit.now())
                        .set(SeriesPO::getMprResponse, JsonKit.toJsonString(response)).
                        set(SeriesPO::getMprErrorMessage, null));

            } else {
                setErrorMprStatus(seriesId, response, "申请Mpr分析失败");
            }
        } catch (Exception e) {
            log.error(e);
            String errorMsg;
            if (e instanceof AppException) {
                errorMsg = ((AppException) e).getErrorMessage();
            } else {
                errorMsg = ExceptionUtil.getRootCauseMessage(e);
            }
            setErrorMprStatus(seriesId, response, errorMsg);
        }

    }

    @Async
    @Override
    public void lockedAsyncDoMprAnalyse(String seriesId) {
        boolean locked = taskLockService.tryLock(TaskType.MPR, seriesId, 2 * 60);
        if (locked) {
            doMprAnalyse(seriesId);
            taskLockService.unlock(TaskType.MPR, seriesId);
        } else {
            log.debug("{} 未获取到锁，忽略执行", seriesId);
        }
    }

    /**
     * 根据accessPath从fs读取文件并压缩
     *
     * @param instancePOList 实例列表
     * @return {@link File }
     * @author zhuhs 2024/07/11 14:39
     */
    private InputStream readDicomFromFSAndZip(List<InstancePO> instancePOList) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            for (InstancePO instancePO : instancePOList) {
                TaFSObject fsObject = fsManager.getObject("mids", instancePO.getAccessPath());
                zipOutputStream.putNextEntry(new ZipEntry(instancePO.getSopInstanceUid()));
                try (InputStream inputStream = fsObject.getInputstream()) {
                    IoUtil.copy(inputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new AppException("读取并压缩DICOM文件异常", e);
        }
        return IoUtil.toStream(outputStream);
    }

    private void setErrorMprStatus(String seriesId, MprResponse mprResponse, String errorMsg) {
        seriesMapper.update(new SeriesPO(), Wrappers.<SeriesPO>lambdaUpdate()
                .eq(SeriesPO::getId, seriesId)
                .set(SeriesPO::getMprStatus, ComputeStatus.COMPUTE_ERROR)
                .set(SeriesPO::getMprResponse, mprResponse == null ? null : JsonKit.toJsonString(mprResponse))
                .set(SeriesPO::getMprErrorMessage, errorMsg));
    }
}
