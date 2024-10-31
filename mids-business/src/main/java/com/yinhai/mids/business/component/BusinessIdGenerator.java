package com.yinhai.mids.business.component;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.mapper.SeqMapper;
import com.yinhai.mids.common.module.mybatis.IBusinessIdGenerator;
import com.yinhai.mids.common.util.DbClock;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zhuhs
 * @date 2024/10/31
 */
@Component
public class BusinessIdGenerator implements IBusinessIdGenerator {

    private static final String CASE = "CASE";
    private static final String STUDY = "STUD";
    private static final String SERIES = "SERI";
    private static final String COMPUTE_SERIES = "COMP";

    @Resource
    private SeqMapper seqMapper;

    @Override
    public String nextId(Object entity) {
        if (entity.getClass().equals(CasePO.class)) {
            return DateUtil.format(DbClock.now(), "yyMMdd") + "00" + StrUtil.padPre(getSeqValue(CASE), 6, '0');
        }
        if (entity.getClass().equals(StudyInfoPO.class)) {
            return DateUtil.format(DbClock.now(), "yyMMdd") + "01" + StrUtil.padPre(getSeqValue(STUDY), 6, '0');
        }
        if (entity.getClass().equals(SeriesInfoPO.class)) {
            return DateUtil.format(DbClock.now(), "yyMMdd") + "02" + StrUtil.padPre(getSeqValue(SERIES), 6, '0');
        }
        if (entity.getClass().equals(ComputeSeriesPO.class)) {
            return DateUtil.format(DbClock.now(), "yyMMdd") + "03" + StrUtil.padPre(getSeqValue(COMPUTE_SERIES), 6, '0');
        }
        return IdWorker.get32UUID();
    }

    private String getSeqValue(String seq) {
        String seqName = seq + DateUtil.format(DbClock.now(), DatePattern.PURE_DATE_PATTERN);
        String seqValue = seqMapper.executeForSequence(seqName);
        if (StrUtil.isEmpty(seqValue) || "0".equals(seqValue)) {
            seqMapper.addSeq(seqName);
            return seqMapper.executeForSequence(seqName);
        } else {
            return seqValue;
        }
    }
}
