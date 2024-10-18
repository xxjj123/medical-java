package com.yinhai.mids.business.service.impl;

import cn.hutool.core.util.ZipUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.mapper.Model3dMapper;
import com.yinhai.mids.business.mapper.SeriesInfoMapper;
import com.yinhai.mids.business.mapper.VtiMapper;
import com.yinhai.mids.business.service.DiagnoseService;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;

@Service
@TaTransactional
public class DiagnoseServiceImpl implements DiagnoseService {
    private static final Log log = LogFactory.get();

    @Resource
    private SeriesInfoMapper seriesInfoMapper;

    @Resource
    private VtiMapper vtiMapper;

    @Resource
    private Model3dMapper model3dMapper;

    @Resource
    private FileStoreService fileStoreService;

    @Override
    public void onMprPush(MultipartFile vtiZip, MultipartFile glbZip, @NotBlank(message = "序列id不能为空") String seriesId, String code, String message) throws IOException {
        // SeriesInfoPO seriesInfo = seriesInfoMapper.selectById(seriesId);
        // AppAssert.notNull(seriesInfo, "该序列不存在!");
        //
        // List<ContextFSObject<String>> contextFSObjects = new ArrayList<>();
        //
        // File unzippedVtiDir = unzipFile(vtiZip);
        // File unzippedGlbDir = unzipFile(glbZip);
        // try {
        //     for (File vtiFile : FileUtil.loopFiles(unzippedVtiDir.getAbsolutePath())) {
        //         ContextFSObject<String> contextFSObject;
        //         try {
        //             contextFSObject = new ContextFSObject<>(vtiFile);
        //             contextFSObject.setContentType("application/vti");
        //         } catch (IOException e) {
        //             log.error("保存dicom文件异常" + e);
        //             throw new AppException("保存dicom文件异常");
        //         }
        //         contextFSObject.setContext(vtiFile.getName());
        //         contextFSObjects.add(contextFSObject);
        //     }
        //
        //     List<ContextUploadResult<String>> contextUploadResults = fileStoreService.upload(contextFSObjects);
        //     List<VtiPO> vtiPOList = new CopyOnWriteArrayList<>();
        //
        //     for (ContextUploadResult<String> contextUploadResult : contextUploadResults) {
        //         VtiPO vtiPO = new VtiPO();
        //         vtiPO.setStudyId(seriesInfo.getStudyId());
        //         vtiPO.setSeriesId(seriesInfo.getSeriesId());
        //         vtiPO.setStudyInstanceUid(seriesInfo.getStudyInstanceUid());
        //         vtiPO.setSeriesInstanceUid(seriesInfo.getSeriesInstanceUid());
        //         String fileName = contextUploadResult.getContext();
        //         String withoutExtension = fileName.substring(0, fileName.lastIndexOf('.'));
        //         String[] parts = withoutExtension.split("_");
        //         String viewName = parts[0];
        //         int viewIndex = Integer.parseInt(parts[1]);
        //         vtiPO.setViewName(viewName);
        //         vtiPO.setViewIndex(viewIndex);
        //         vtiPO.setAccessPath(contextUploadResult.getAccessPath());
        //         vtiPOList.add(vtiPO);
        //     }
        //
        //
        //     List<File> Model3dFiles = FileUtil.loopFiles(unzippedGlbDir.getAbsolutePath());
        //     AppAssert.equals(Model3dFiles.size(), 1, "生成模型数量不为1");
        //
        //     File modelFile = Model3dFiles.get(0);
        //     Model3dPO model3dPO = new Model3dPO();
        //     try {
        //         ContextFSObject<File> fsObject = new ContextFSObject<>(modelFile);
        //         fsObject.setContentType("application/glb");
        //         UploadResult uploadResult = fileStoreService.upload(fsObject);
        //         model3dPO.setType("bone");
        //         model3dPO.setStudyId(seriesInfo.getStudyId());
        //         model3dPO.setSeriesId(seriesInfo.getSeriesId());
        //         model3dPO.setStudyInstanceUid(seriesInfo.getStudyInstanceUid());
        //         model3dPO.setSeriesInstanceUid(seriesInfo.getSeriesInstanceUid());
        //         model3dPO.setAccessPath(uploadResult.getAccessPath());
        //     } catch (IOException e) {
        //         throw new AppException("上传3d文件异常");
        //     }
        //
        //     Integer axialCount = CollUtil.count(vtiPOList, e -> StrUtil.equals("axial", e.getViewName()));
        //     Integer coronalCount = CollUtil.count(vtiPOList, e -> StrUtil.equals("sagittal", e.getViewName()));
        //     Integer sagittalCount = CollUtil.count(vtiPOList, e -> StrUtil.equals("coronal", e.getViewName()));
        //
        //     System.out.println(model3dPO);
        //     seriesInfoMapper.updateById(new SeriesInfoPO().setSeriesId(seriesId)
        //             .setMprStatus(ComputeStatus.COMPUTE_SUCCESS).setAxialCount(axialCount).setCoronalCount(coronalCount).setSagittalCount(sagittalCount));
        //     vtiMapper.insertBatch(vtiPOList);
        //     model3dMapper.insert(model3dPO);
        //
        // } catch (Exception e) {
        //     String errorMsg;
        //     if (e instanceof AppException) {
        //         errorMsg = ((AppException) e).getErrorMessage();
        //     } else {
        //         errorMsg = ExceptionUtil.getRootCauseMessage(e);
        //     }
        //     seriesInfoMapper.updateById(new SeriesInfoPO().setSeriesId(seriesId)
        //             .setMprErrorMessage(errorMsg)
        //             .setMprStatus(ComputeStatus.COMPUTE_ERROR));
        // } finally {
        //     FileUtil.del(unzippedVtiDir);
        //     FileUtil.del(unzippedGlbDir);
        // }
    }

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
}
