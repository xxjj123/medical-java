package com.yinhai.mids.business.service.impl;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.base.Joiner;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.entity.vo.ImageInitInfoVO;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.mids.business.service.ImageService;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.core.utils.ResponseExportUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author zhuhs
 * @date 2024/8/27
 */
@Service
@TaTransactional
public class ImageServiceImpl implements ImageService {

    private static final Log log = LogFactory.get();

    @Resource
    private StudyInfoMapper studyInfoMapper;

    @Resource
    private SeriesInfoMapper seriesInfoMapper;

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private MprSliceMapper mprSliceMapper;

    @Resource
    private MprModelMapper mprModelMapper;

    @Resource
    private FileStoreService fileStoreService;

    @Override
    @SuppressWarnings("unchecked")
    public ImageInitInfoVO queryInitInfo(String computeSeriesId) {
        ComputeSeriesPO computeSeries = computeSeriesMapper.selectOne(Wrappers.<ComputeSeriesPO>lambdaQuery()
                .select(ComputeSeriesPO::getStudyId, ComputeSeriesPO::getSeriesId, ComputeSeriesPO::getComputeStatus)
                .eq(ComputeSeriesPO::getComputeSeriesId, computeSeriesId));
        AppAssert.notNull(computeSeries, "该序列不存在！");
        SeriesInfoPO seriesInfo = seriesInfoMapper.selectById(computeSeries.getSeriesId());
        AppAssert.notNull(seriesInfo, "该序列不存在");
        StudyInfoPO studyInfo = studyInfoMapper.selectById(computeSeries.getStudyId());
        AppAssert.notNull(studyInfo, "该序列对应检查不存在！");
        AppAssert.equals(computeSeries.getComputeStatus(), 3, "该序列计算不成功");

        ImageInitInfoVO result = new ImageInitInfoVO();
        result.setComputeSeriesId(computeSeriesId);
        result.setStudyId(studyInfo.getStudyId());
        result.setSeriesId(seriesInfo.getSeriesId());
        result.setImageCount(seriesInfo.getImageCount());

        Map<String, Integer> viewTotal = mprSliceMapper.queryViewTotal(seriesInfo.getSeriesId());
        result.setAxialCount(viewTotal.getOrDefault("axial", 0));
        result.setCoronalCount(viewTotal.getOrDefault("coronal", 0));
        result.setSagittalCount(viewTotal.getOrDefault("sagittal", 0));

        result.setSliceThickness(studyInfo.getSliceThickness());
        result.setKvp(studyInfo.getKvp());
        result.setPixelSpacing(studyInfo.getPixelSpacing());
        result.setInstitutionName(studyInfo.getInstitutionName());
        result.setManufacturer(studyInfo.getManufacturer());
        result.setPatientName(studyInfo.getPatientName());
        result.setPatientSex(studyInfo.getPatientSex());
        result.setPatientAge(studyInfo.getPatientAge());
        result.setPatientId(studyInfo.getPatientId());
        result.setStudyDateAndTime(studyInfo.getStudyDateAndTime());
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void downloadSlice(String seriesId, String viewName, Integer viewIndex, HttpServletResponse response) {
        MprSlicePO slice = mprSliceMapper.selectOne(Wrappers.<MprSlicePO>lambdaQuery()
                .select(MprSlicePO::getAccessPath)
                .eq(MprSlicePO::getSeriesId, seriesId)
                .eq(MprSlicePO::getViewName, viewName)
                .eq(MprSlicePO::getViewIndex, viewIndex));
        AppAssert.notNull(slice, "未找到切片");
        try (InputStream in = fileStoreService.download(slice.getAccessPath())) {
            ResponseExportUtil.exportFileWithStream(response, in, Joiner.on(".").join(seriesId, viewName, viewIndex, "slice"));
        } catch (IOException e) {
            log.error(e, "下载影像切片异常，seriesId = {}, viewName = {}, viewIndex = {}", seriesId, viewName, viewIndex);
            throw new AppException("下载切片异常");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void download3dModel(String seriesId, HttpServletResponse response) {
        MprModelPO model = mprModelMapper.selectOne(Wrappers.<MprModelPO>lambdaQuery()
                .select(MprModelPO::getAccessPath)
                .eq(MprModelPO::getSeriesId, seriesId)
                .eq(MprModelPO::getMprType, "bone"));
        AppAssert.notNull(model, "未找到3D模型");

        try (InputStream in = fileStoreService.download(model.getAccessPath())) {
            ResponseExportUtil.exportFileWithStream(response, in, Joiner.on(".").join(seriesId, "bone", "3d"));
        } catch (IOException e) {
            log.error(e, "下载3D模型异常，seriesId = {}, ", seriesId);
            throw new AppException("下载3D模型异常");
        }
    }
}
