package com.yinhai.mids.business.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.yinhai.mids.business.constant.ComputeStatus;
import com.yinhai.mids.business.entity.model.ContextFSObject;
import com.yinhai.mids.business.entity.model.ContextUploadResult;
import com.yinhai.mids.business.entity.model.UploadResult;
import com.yinhai.mids.business.entity.po.*;
import com.yinhai.mids.business.entity.vo.FocalDetailVO;
import com.yinhai.mids.business.entity.vo.FocalVO;
import com.yinhai.mids.business.entity.vo.SeriesVO;
import com.yinhai.mids.business.mapper.*;
import com.yinhai.mids.business.service.DiagnoseService;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.module.storage.core.ITaFSManager;
import com.yinhai.ta404.module.storage.core.TaFSObject;
import com.yinhai.ta404.storage.ta.core.FSManager;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@TaTransactional
public class DiagnoseServiceImpl implements DiagnoseService {
    private static final Log log = LogFactory.get();

    @Resource
    private SeriesMapper seriesMapper;

    @Resource
    private ComputeSeriesMapper computeSeriesMapper;

    @Resource
    private DiagMapper diagMapper;

    @Resource
    private VolumeDetailMapper volumeDetailMapper;

    @Resource
    private InstanceMapper instanceMapper;

    @Resource
    private VtiMapper vtiMapper;

    @Resource
    private Model3dMapper model3dMapper;

    @Resource
    private DetailAnnotationMapper detailAnnotationMapper;

//    @Resource(name = "uploadThreadPool")
//    private ThreadPoolTaskExecutor uploadThreadPool;

    @Resource
    private FileStoreService fileStoreService;

    @Resource
    private ITaFSManager<FSManager> fsManager;


    @Override
    public FocalVO getNoduleInfo(@NotNull(message = "序列id不能为空") String computeSeriesId) {
        ComputeSeriesPO computeSeriesPO = computeSeriesMapper.selectById(computeSeriesId);
        AppAssert.notNull(computeSeriesPO, "该序列不存在! ");

        SeriesPO seriesPO = seriesMapper.selectById(computeSeriesPO.getSeriesId());
        AppAssert.notNull(seriesPO, "该序列不存在! ");

        String computeStatus = computeSeriesPO.getComputeStatus();
        if (StrUtil.equals(computeStatus, ComputeStatus.COMPUTE_SUCCESS)) {
            DiagPO noduleDiag = diagMapper.selectOne(Wrappers.<DiagPO>lambdaQuery().eq(DiagPO::getType, "nodule").eq(DiagPO::getApplyId, computeSeriesPO.getApplyId()));
            AppAssert.notNull(noduleDiag, "结节诊断数据不存在");

            FocalVO focalVO = new FocalVO();
            focalVO.setDiagnosis(noduleDiag.getDiagnosis());
            focalVO.setFinding(noduleDiag.getFinding());
            focalVO.setNumber(noduleDiag.getNumber());
            focalVO.setHasLesion(noduleDiag.getHasLesion());
            focalVO.setSeriesUid(noduleDiag.getSeriesUid());

            List<FocalDetailVO> focalDetailVOList = getFocalDetailVOList(noduleDiag.getId());

            focalVO.setFocalDetailList(focalDetailVOList);
            return focalVO;
        } else if (StrUtil.equals(computeStatus, ComputeStatus.WAIT_COMPUTE)) {
            throw new AppException("等待计算中...");
        } else if (StrUtil.equals(computeStatus, ComputeStatus.IN_COMPUTE)) {
            throw new AppException("计算中...");
        } else if (StrUtil.equals(computeStatus, ComputeStatus.COMPUTE_FAILED)) {
            throw new AppException("计算失败...");
        } else if (StrUtil.equals(computeStatus, ComputeStatus.COMPUTE_CANCELED)) {
            throw new AppException("计算取消...");
        } else if (StrUtil.equals(computeStatus, ComputeStatus.COMPUTE_ERROR)) {
            throw new AppException("计算异常...");
        }

        return null;
    }

    ;

    /**
     * 获取结节详细信息列表
     */
    private List<FocalDetailVO> getFocalDetailVOList(String noduleDiagId) {
        List<FocalDetailVO> noduleDetailVOList = new ArrayList<>();
        List<FocalDetailPO> focalDetailPOList = volumeDetailMapper.selectList(Wrappers.<FocalDetailPO>lambdaQuery().eq(FocalDetailPO::getDiagId, noduleDiagId));
        for (FocalDetailPO focalDetailPO : focalDetailPOList) {
            FocalDetailVO focalDetail = new FocalDetailVO();
            focalDetail.setSopInstanceUid(focalDetailPO.getSopInstanceUid());
            focalDetail.setVocabularyEntry(focalDetailPO.getVocabularyEntry());
            focalDetail.setType(focalDetailPO.getType());
            focalDetail.setBoxIndex(focalDetailPO.getBoxIndex());
            focalDetail.setLobeSegment(focalDetailPO.getLobeSegment());
            focalDetail.setLobe(focalDetailPO.getLobe());
            focalDetail.setVolume(focalDetailPO.getVolume());
            focalDetail.setRiskCode(focalDetailPO.getRiskCode());
            FocalDetailVO.CtMeasures ctMeasures = new FocalDetailVO.CtMeasures();
            ctMeasures.setMaximum(focalDetailPO.getCtMeasuresMaximum());
            ctMeasures.setMean(focalDetailPO.getCtMeasuresMean());
            ctMeasures.setMinimum(focalDetailPO.getCtMeasuresMinimum());
            focalDetail.setCtMeasures(ctMeasures);

            FocalDetailVO.EllipsoidAxis ellipsoidAxis = new FocalDetailVO.EllipsoidAxis();
            ellipsoidAxis.setLeast(focalDetailPO.getEllipsoidAxisLeast());
            ellipsoidAxis.setMinor(focalDetailPO.getEllipsoidAxisMinor());
            ellipsoidAxis.setMajor(focalDetailPO.getEllipsoidAxisMajor());
            focalDetail.setEllipsoidAxis(ellipsoidAxis);

            List<FocalAnnoPO> detailAnnotationPOList = detailAnnotationMapper.selectList(Wrappers.<FocalAnnoPO>lambdaQuery().eq(FocalAnnoPO::getFocalDetailId, focalDetailPO.getId()));
            boolean isNumAvailable = !CollUtil.isEmpty(detailAnnotationPOList) && detailAnnotationPOList.size() == 2;
            AppAssert.isTrue(isNumAvailable, "坐标点数量错误");

            Integer[] bbox = new Integer[6];
            FocalAnnoPO point1 = detailAnnotationPOList.get(0);
            FocalAnnoPO point2 = detailAnnotationPOList.get(1);
            bbox[0] = Math.min(point1.getPointX(), point2.getPointX());
            bbox[1] = Math.max(point1.getPointX(), point2.getPointX());
            bbox[2] = Math.min(point1.getPointY(), point2.getPointY());
            bbox[3] = Math.max(point1.getPointY(), point2.getPointY());
            bbox[4] = Math.min(point1.getPointZ(), point2.getPointZ());
            bbox[5] = Math.max(point1.getPointZ(), point2.getPointZ());
            focalDetail.setBbox(bbox);

            noduleDetailVOList.add(focalDetail);
        }
        return noduleDetailVOList;

    }


    @Override
    public InputStream downloadDicomZip(@NotNull(message = "序列id不能为空") String computeSeriesId) {
        ComputeSeriesPO computeSeriesPO = computeSeriesMapper.selectById(computeSeriesId);
        AppAssert.notNull(computeSeriesPO, "该序列不存在! ");

        LambdaQueryWrapper<InstancePO> queryWrapper = Wrappers.<InstancePO>lambdaQuery()
                .select(InstancePO::getAccessPath, InstancePO::getSopInstanceUid)
                .eq(InstancePO::getSeriesId, computeSeriesPO.getSeriesId());
        List<InstancePO> instancePOList = instanceMapper.selectList(queryWrapper);

        AppAssert.notEmpty(instancePOList, "未读取到dicom实例信息");
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
            for (InstancePO instancePO : instancePOList) {
                TaFSObject fsObject = fsManager.getObject("mids", instancePO.getAccessPath());
                zipOutputStream.putNextEntry(new ZipEntry(instancePO.getSopInstanceUid()));
                try (InputStream inputStream = fsObject.getInputstream()) {
                    IoUtil.copy(inputStream, zipOutputStream);
                }
                zipOutputStream.closeEntry();
            }
            return IoUtil.toStream(outputStream);
        } catch (IOException e) {
            throw new AppException("读取并压缩DICOM文件异常");
        }


    }

    @Override
    public InputStream downSlice(@NotBlank(message = "序列id不能为空") String seriesId, @NotBlank(message = "viewname不能为空") String viewName, @NotNull(message = "viewindex不能为空") Integer viewIndex) {
        VtiPO vtiPO = vtiMapper.selectOne(Wrappers.<VtiPO>lambdaQuery().eq(VtiPO::getSeriesId, seriesId).eq(VtiPO::getViewName, viewName).eq(VtiPO::getViewIndex, viewIndex));
        AppAssert.notNull(vtiPO, "未找到切片");
        try {
            TaFSObject fsObject = fsManager.getObject("mids", vtiPO.getAccessPath());
            AppAssert.notNull(fsObject, "文件下载异常");
            return fsObject.getInputstream();
        } catch (Exception e) {
            throw new AppException("读取vti文件异常");
        }
    }

    ;

    @Override
    public void onMprPush(MultipartFile vtiZip, MultipartFile glbZip,@NotBlank(message = "序列id不能为空") String seriesId, String code, String message) throws IOException {
        SeriesPO seriesPO = seriesMapper.selectById(seriesId);
        AppAssert.notNull(seriesPO, "该序列不存在!");

        try (InputStream inputStream = vtiZip.getInputStream()) {
            AppAssert.equals("zip", FileTypeUtil.getType(inputStream), "只允许上传vti zip文件");
        }

        try (InputStream inputStream = glbZip.getInputStream()) {
            AppAssert.equals("zip", FileTypeUtil.getType(inputStream), "只允许上传glb zip文件");
        }

        List<ContextFSObject<String>> contextFSObjects = new ArrayList<>();

        File unzippedVtiDir = unzipFile(vtiZip);
        File unzippedGlbDir = unzipFile(glbZip);
        try {
            for (File vtiFile : FileUtil.loopFiles(unzippedVtiDir.getAbsolutePath())) {
                ContextFSObject<String> contextFSObject;
                try {
                    contextFSObject = new ContextFSObject<>(vtiFile);
                } catch (IOException e) {
                    log.error("保存dicom文件异常" + e);
                    throw new AppException("保存dicom文件异常");
                }
                contextFSObject.setContext(vtiFile.getName());
                contextFSObjects.add(contextFSObject);
            }

            fileStoreService.upload(contextFSObjects);
            List<ContextUploadResult<String>> contextUploadResults = fileStoreService.upload(contextFSObjects);

            List<VtiPO> vtiPOList = new CopyOnWriteArrayList<>();

            for(ContextUploadResult<String> contextUploadResult:  contextUploadResults){
                VtiPO vtiPO = new VtiPO();
                vtiPO.setStudyId(seriesPO.getStudyId());
                vtiPO.setSeriesId(seriesPO.getId());
                vtiPO.setStudyInstanceUid(seriesPO.getStudyInstanceUid());
                vtiPO.setSeriesInstanceUid(seriesPO.getSeriesInstanceUid());
                String fileName = contextUploadResult.getContext();
                String withoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
                String[] parts = withoutExtension.split("_");
                String viewName = parts[0];
                int viewIndex = Integer.parseInt(parts[1]);
                vtiPO.setViewName(viewName);
                vtiPO.setViewIndex(viewIndex);
                vtiPO.setAccessPath(contextUploadResult.getAccessPath());
                vtiPOList.add(vtiPO);
            }


            List<File> Model3dFiles = FileUtil.loopFiles(unzippedGlbDir.getAbsolutePath());
            AppAssert.equals(Model3dFiles.size(),1,"生成模型数量不为1");

            File modelFile =  Model3dFiles.get(0);
            Model3dPO model3dPO = new Model3dPO();
            try {
                ContextFSObject<File> fsObject = new ContextFSObject<>(modelFile);
                UploadResult uploadResult = fileStoreService.upload(fsObject);
                model3dPO.setType("bone");
                model3dPO.setStudyId(seriesPO.getStudyId());
                model3dPO.setSeriesId(seriesPO.getId());
                model3dPO.setStudyInstanceUid(seriesPO.getStudyInstanceUid());
                model3dPO.setSeriesInstanceUid(seriesPO.getSeriesInstanceUid());
                model3dPO.setAccessPath(uploadResult.getAccessPath());
            } catch (IOException e) {
                throw new AppException("上传3d文件异常");
            }

            Integer axialCount = CollUtil.count(vtiPOList, e -> StrUtil.equals("axial", e.getViewName()));
            Integer coronalCount = CollUtil.count(vtiPOList, e -> StrUtil.equals("sagittal", e.getViewName()));
            Integer sagittalCount = CollUtil.count(vtiPOList, e -> StrUtil.equals("coronal", e.getViewName()));

            System.out.println(model3dPO);
            seriesMapper.updateById(new SeriesPO().setId(seriesId)
                    .setMprStatus(ComputeStatus.COMPUTE_SUCCESS).setAxialCount(axialCount).setCoronalCount(coronalCount).setSagittalCount(sagittalCount));
            vtiMapper.insertBatch(vtiPOList);
            model3dMapper.insert(model3dPO);

        } catch (Exception e) {
            String errorMsg;
            if (e instanceof AppException) {
                errorMsg = ((AppException) e).getErrorMessage();
            } else {
                errorMsg = ExceptionUtil.getRootCauseMessage(e);
            }
            seriesMapper.updateById(new SeriesPO().setId(seriesId)
                    .setMprErrorMessage(errorMsg)
                    .setMprStatus(ComputeStatus.COMPUTE_ERROR));
        } finally {
            FileUtil.del(unzippedVtiDir);
            FileUtil.del(unzippedGlbDir);

        }
    }



//        private <T> List<VtiPO> upload(List<ContextFSObject<T>> fsObjects, SeriesPO seriesPO) {
//        List<VtiPO> vtiPOList = new CopyOnWriteArrayList<>();
//        CompletionService<Void> completionService = new ExecutorCompletionService<>(uploadThreadPool);
//        for (ContextFSObject<T> fsObject : fsObjects) {
//            completionService.submit(() -> {
//                TaFSObject taFSObject = fsManager.putObject("mids", fsObject);
//                fsObject.setKeyId(taFSObject.getKeyId());
//                VtiPO vtiPO = new VtiPO();
//                vtiPO.setStudyId(seriesPO.getStudyId());
//                vtiPO.setSeriesId(seriesPO.getId());
//                vtiPO.setStudyInstanceUid(seriesPO.getStudyInstanceUid());
//                vtiPO.setSeriesInstanceUid(seriesPO.getSeriesInstanceUid());
//                String fileName = (String) fsObject.getContext();
//                String withoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
//                String[] parts = withoutExtension.split("_");
//                String viewName = parts[0];
//                int viewIndex = Integer.parseInt(parts[1]);
//                vtiPO.setViewName(viewName);
//                vtiPO.setViewIndex(viewIndex);
//                vtiPO.setAccessPath(fsObject.getKeyId());
//                vtiPOList.add(vtiPO);
//                return null;
//            });
//        }
//        for (ContextFSObject<T> fsObject : fsObjects) {
//            try {
//                completionService.take().get();
//            } catch (Exception e) {
//                log.error(e);
//                throw new AppException("文件上传失败！");
//            }
//        }
//        return vtiPOList;
//
//    }


    private File unzipFile(MultipartFile zipFile) {
        File tempDir;
        try {
            tempDir = Files.createTempDirectory("vti").toFile();
        } catch (IOException e) {
            log.error(e);
            throw new AppException("创建临时文件异常");
        }
        try (InputStream inputStream = zipFile.getInputStream()) {
            ZipUtil.unzip(inputStream, tempDir, Charset.defaultCharset());
        } catch (IOException e) {
            log.error(e);
            throw new AppException("读取Vti文件内容异常");
        }
        return tempDir;
    }



    @Override
    public SeriesVO getSeriesInfo(String computeSeriesId) {
        ComputeSeriesPO computeSeriesPO = computeSeriesMapper.selectById(computeSeriesId);
        AppAssert.notNull(computeSeriesPO, "该计算序列不存在!");

        SeriesPO seriesPO = seriesMapper.selectOne(Wrappers.<SeriesPO>lambdaQuery().eq(SeriesPO::getId, computeSeriesPO.getSeriesId()));
        AppAssert.notNull(seriesPO, StrUtil.format("计算序列{}对应序列不存在", computeSeriesId));

        String mprStatus = seriesPO.getMprStatus();
        AppAssert.notNull(mprStatus, "没有mpr处理过");
        System.out.println(mprStatus);
        System.out.println(StrUtil.equals(mprStatus, ComputeStatus.COMPUTE_SUCCESS));

        if (StrUtil.equals(mprStatus, ComputeStatus.COMPUTE_SUCCESS)) {
            SeriesVO seriesVO = new SeriesVO();
            seriesVO.setSeriesId(seriesPO.getId());
            seriesVO.setImageCount(seriesPO.getImageCount());
            seriesVO.setAxialCount(seriesPO.getAxialCount());
            seriesVO.setMprStatus(seriesPO.getMprStatus());
            seriesVO.setCoronalCount(seriesPO.getCoronalCount());
            seriesVO.setSagittalCount(seriesPO.getSagittalCount());
            return seriesVO;
        } else if (StrUtil.equals(mprStatus, ComputeStatus.WAIT_COMPUTE)) {
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

        return null;
    }


}
