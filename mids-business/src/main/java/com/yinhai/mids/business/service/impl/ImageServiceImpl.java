package com.yinhai.mids.business.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.po.ComputeSeriesPO;
import com.yinhai.mids.business.entity.po.SeriesPO;
import com.yinhai.mids.business.entity.po.StudyPO;
import com.yinhai.mids.business.entity.vo.ImageInitInfoVO;
import com.yinhai.mids.business.mapper.ComputeSeriesMapper;
import com.yinhai.mids.business.mapper.SeriesMapper;
import com.yinhai.mids.business.mapper.StudyMapper;
import com.yinhai.mids.business.service.ImageService;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author zhuhs
 * @date 2024/8/27
 */
@Service
@TaTransactional
public class ImageServiceImpl implements ImageService {

    @Resource
    private StudyMapper studyMapper;

    @Resource
    private SeriesMapper seriesMapper;

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

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
}
