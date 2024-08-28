package com.yinhai.mids.business.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.base.Joiner;
import com.yinhai.mids.business.constant.ComputeStatus;
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

/**
 * @author zhuhs
 * @date 2024/8/27
 */
@Service
@TaTransactional
public class ImageServiceImpl implements ImageService {

    private static final Log log = LogFactory.get();

    @Resource
    private StudyMapper studyMapper;

    @Resource
    private SeriesMapper seriesMapper;

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private VtiMapper vtiMapper;

    @Resource
    private Model3dMapper model3dMapper;

    @Resource
    private FileStoreService fileStoreService;

    @Override
    @SuppressWarnings("unchecked")
    public ImageInitInfoVO queryInitInfo(String computeSeriesId) {
        ComputeSeriesPO computeSeriesPO = computeSeriesMapper.selectOne(Wrappers.<ComputeSeriesPO>lambdaQuery()
                .select(ComputeSeriesPO::getStudyId, ComputeSeriesPO::getSeriesId)
                .eq(ComputeSeriesPO::getId, computeSeriesId));
        AppAssert.notNull(computeSeriesPO, "该序列不存在！");
        SeriesPO seriesPO = seriesMapper.selectById(computeSeriesPO.getSeriesId());
        AppAssert.notNull(seriesPO, "该序列不存在");
        StudyPO studyPO = studyMapper.selectById(computeSeriesPO.getStudyId());
        AppAssert.notNull(studyPO, "该序列对应检查不存在！");

        String mprStatus = seriesPO.getMprStatus();
        if (StrUtil.equals(mprStatus, ComputeStatus.WAIT_COMPUTE)) {
            throw new AppException("mpr等待计算中...");
        } else if (StrUtil.equals(mprStatus, ComputeStatus.IN_COMPUTE)) {
            throw new AppException("mpr计算中...");
        } else if (StrUtil.equals(mprStatus, ComputeStatus.COMPUTE_FAILED)) {
            throw new AppException("mpr计算失败...");
        } else if (StrUtil.equals(mprStatus, ComputeStatus.COMPUTE_CANCELED)) {
            throw new AppException("mpr计算取消...");
        } else if (StrUtil.equals(mprStatus, ComputeStatus.COMPUTE_ERROR)) {
            throw new AppException("mpr计算异常...");
        }

        ImageInitInfoVO result = new ImageInitInfoVO();
        result.setComputeSeriesId(computeSeriesId);
        result.setStudyId(studyPO.getId());
        result.setSeriesId(seriesPO.getId());
        result.setImageCount(seriesPO.getImageCount());
        result.setAxialCount(seriesPO.getAxialCount());
        result.setCoronalCount(seriesPO.getCoronalCount());
        result.setSagittalCount(seriesPO.getSagittalCount());
        result.setSliceThickness(studyPO.getSliceThickness());
        result.setKvp(studyPO.getKvp());
        result.setPixelSpacing(studyPO.getPixelSpacing());
        result.setInstitutionName(studyPO.getInstitutionName());
        result.setManufacturer(studyPO.getManufacturer());
        result.setPatientSex(studyPO.getPatientSex());
        result.setPatientAge(studyPO.getPatientAge());
        result.setPatientId(studyPO.getPatientId());
        result.setStudyDateAndTime(studyPO.getStudyDateAndTime());
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void downloadSlice(String seriesId, String viewName, Integer viewIndex, HttpServletResponse response) {
        VtiPO vtiPO = vtiMapper.selectOne(Wrappers.<VtiPO>lambdaQuery()
                .select(VtiPO::getAccessPath)
                .eq(VtiPO::getSeriesId, seriesId)
                .eq(VtiPO::getViewName, viewName)
                .eq(VtiPO::getViewIndex, viewIndex));
        AppAssert.notNull(vtiPO, "未找到切片");

        try (InputStream in = fileStoreService.download(vtiPO.getAccessPath())) {
            ResponseExportUtil.exportFileWithStream(response, in, Joiner.on(".").join(seriesId, viewName, viewIndex, "slice"));
        } catch (IOException e) {
            log.error(e, "下载影像切片异常，seriesId = {}, viewName = {}, viewIndex = {}", seriesId, viewName, viewIndex);
            throw new AppException("下载切片异常");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void download3dModel(String seriesId, HttpServletResponse response) {
        Model3dPO model3dPO = model3dMapper.selectOne(Wrappers.<Model3dPO>lambdaQuery()
                .select(Model3dPO::getAccessPath)
                .eq(Model3dPO::getSeriesId, seriesId)
                .eq(Model3dPO::getType, "bone"));
        AppAssert.notNull(model3dPO, "未找到3d模型");

        try (InputStream in = fileStoreService.download(model3dPO.getAccessPath())) {
            ResponseExportUtil.exportFileWithStream(response, in, Joiner.on(".").join(seriesId, "bone", "3d"));
        } catch (IOException e) {
            log.error(e, "下载3D模型异常，seriesId = {}, ", seriesId);
            throw new AppException("下载切片异常");
        }
    }
}
