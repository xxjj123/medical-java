package com.yinhai.mids.business.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.model.ContextFSObject;
import com.yinhai.mids.business.entity.model.UploadResult;
import com.yinhai.mids.business.entity.po.InstancePO;
import com.yinhai.mids.business.entity.po.SeriesPO;
import com.yinhai.mids.business.entity.po.VtiPO;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.mpr.MprClient;
import com.yinhai.mids.business.mpr.MprProperties;
import com.yinhai.mids.business.mpr.MprResponse;
import com.yinhai.mids.business.mpr.RegisterParam;
import com.yinhai.mids.business.service.AnalyseService;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.mids.common.util.JsonKit;
import com.yinhai.mids.common.util.MapperKit;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author zhuhs
 * @date 2024/7/18 15:25
 */
@Service
@TaTransactional
public class AnalyseServiceImpl implements AnalyseService {

    private static final Log log = LogFactory.get();

    @Resource
    private VtiMapper vtiMapper;


    @Resource
    private FileStoreService fileStoreService;

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private SeriesMapper seriesMapper;

    @Resource
    private StudyMapper studyMapper;

    @Resource
    private InstanceMapper instanceMapper;

    @Resource
    private MprClient mprClient;

    @Resource
    private MprProperties mprProperties;

    @Resource
    private ITaFSManager<FSManager> fsManager;


    @Override
    public void uploadVti(String viewName, int viewIndex, File vtiFile) {
        VtiPO vtiPO = new VtiPO();
        vtiPO.setStudyId("1813835186748583938");
        vtiPO.setSeriesId("1813835186798915586");
        vtiPO.setStudyInstanceUid("1.2.392.200036.9125.2.138612190166.20210407000133");
        vtiPO.setSeriesInstanceUid("1.2.840.113619.2.289.3.168430441.447.1617294423.131.3");
        vtiPO.setViewName(viewName);
        vtiPO.setViewIndex(viewIndex);
        try {
            ContextFSObject<File> fsObject = new ContextFSObject<>(vtiFile);
            fsObject.setContentType("application/octet-stream");
            UploadResult uploadResult = fileStoreService.upload(fsObject);
            vtiPO.setAccessPath(uploadResult.getAccessPath());
        } catch (IOException e) {
            throw new AppException("上传文件异常");
        }
        vtiMapper.insert(vtiPO);
    }

    @Override
    public void view(String id, HttpServletResponse response) {
        VtiPO vtiPO = vtiMapper.selectById(id);
        try (InputStream inputStream = fileStoreService.download(vtiPO.getAccessPath());
             OutputStream outputStream = response.getOutputStream()) {
            response.setContentType("application/octet-stream");
            IoUtil.copy(inputStream, outputStream);
        } catch (IOException e) {
            log.error(e, "下载vti文件异常");
            throw new AppException("文件服务异常");
        }
    }

    @Override
    public void doMprAnalyse(String seriesId) {
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
                    .setMprStartTime(MapperKit.executeForDate())
                    .setMprErrorMessage(StrUtil.format("序列对应实例不存在", seriesId)));
            throw new AppException("序列对应实例不存在");
        }
        RegisterParam registerParam = new RegisterParam();
        registerParam.setSeriesId(seriesPO.getId());
        registerParam.setCallbackUrl(mprProperties.getPushCallbackUrl());
        MprResponse response = null;
        try (InputStream inputStream = readDicomFromFSAndZip(instancePOList)) {
            System.out.println(registerParam);
            response = mprClient.register(mprProperties.getRegisterUrl(), inputStream, registerParam);
            if (response.getCode() == 1) {
                seriesMapper.updateById(new SeriesPO().setId(seriesId)
                        .setMprStatus(ComputeStatus.IN_COMPUTE)
                        .setMprStartTime(MapperKit.executeForDate())
                        .setMprResponse(JsonKit.toJsonString(response)));

            } else {
                seriesMapper.updateById(new SeriesPO().setId(seriesId)
                        .setMprErrorMessage("申请Mpr分析失败")
                        .setMprStartTime(MapperKit.executeForDate())
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
                    .setMprStartTime(MapperKit.executeForDate())
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
