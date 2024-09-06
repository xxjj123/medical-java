package com.yinhai.mids.business.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dtflys.forest.http.ForestResponse;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.InstancePO;
import com.yinhai.mids.business.entity.po.SeriesPO;
import com.yinhai.mids.business.mapper.InstanceMapper;
import com.yinhai.mids.business.mapper.SeriesMapper;
import com.yinhai.mids.business.mpr.MprClient;
import com.yinhai.mids.business.mpr.MprProperties;
import com.yinhai.mids.business.mpr.MprResponse;
import com.yinhai.mids.business.mpr.RegisterParam;
import com.yinhai.mids.business.service.MprService;
import com.yinhai.mids.common.util.JsonKit;
import com.yinhai.mids.common.util.DbKit;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
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

    @Override
    @SuppressWarnings("unchecked")
    public void doMprAnalyse(String seriesId) {
        ForestResponse<MprResponse> connectResponse = mprClient.testConnect(mprProperties.getRegisterUrl());
        if (connectResponse.isError()) {
            log.error("连接MPR服务失败，请检查网络配置或MPR服务是否正常");
            return;
        }

        SeriesPO seriesPO = seriesMapper.selectById(seriesId);
        if (seriesPO == null) {
            log.error("序列不存在", seriesId);
            throw new AppException("序列不存在");
        }
        LambdaQueryWrapper<InstancePO> queryWrapper = Wrappers.<InstancePO>lambdaQuery()
                .select(InstancePO::getAccessPath, InstancePO::getSopInstanceUid)
                .eq(InstancePO::getSeriesId, seriesId);
        List<InstancePO> instancePOList = instanceMapper.selectList(queryWrapper);
        if (CollUtil.isEmpty(instancePOList)) {
            log.error("序列对应实例不存在", seriesId);
            seriesMapper.updateById(new SeriesPO().setId(seriesId)
                    .setMprStatus(ComputeStatus.COMPUTE_ERROR)
                    .setMprStartTime(DbKit.now())
                    .setMprErrorMessage(StrUtil.format("序列对应实例不存在", seriesId)));
            throw new AppException("序列对应实例不存在");
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
                seriesMapper.updateById(new SeriesPO().setId(seriesId)
                        .setMprStatus(ComputeStatus.IN_COMPUTE)
                        .setMprStartTime(DbKit.now())
                        .setMprResponse(JsonKit.toJsonString(response)));

            } else {
                seriesMapper.updateById(new SeriesPO().setId(seriesId)
                        .setMprErrorMessage("申请Mpr分析失败")
                        .setMprStartTime(DbKit.now())
                        .setMprStatus(ComputeStatus.COMPUTE_ERROR)
                        .setMprResponse(JsonKit.toJsonString(response)));
            }
        } catch (Exception e) {
            log.error(e);
            String errorMsg;
            if (e instanceof AppException) {
                errorMsg = ((AppException) e).getErrorMessage();
            } else {
                errorMsg = ExceptionUtil.getRootCauseMessage(e);
            }
            seriesMapper.updateById(new SeriesPO().setId(seriesId)
                    .setMprErrorMessage(errorMsg)
                    .setMprStartTime(DbKit.now())
                    .setMprStatus(ComputeStatus.COMPUTE_ERROR)
                    .setMprResponse(response != null ? JsonKit.toJsonString(response) : null));
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

}
