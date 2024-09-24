package com.yinhai.mids.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.base.Joiner;
import com.yinhai.mids.business.entity.model.ContextFSObject;
import com.yinhai.mids.business.entity.model.DicomInfo;
import com.yinhai.mids.business.entity.po.SingleImageStudyPO;
import com.yinhai.mids.business.entity.vo.SingleImageInfoVO;
import com.yinhai.mids.business.mapper.SingleImageStudyMapper;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.mids.business.service.SingleImageService;
import com.yinhai.mids.business.util.DicomUtil;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.DbKit;
import com.yinhai.mids.common.util.SecurityKit;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.core.utils.ResponseExportUtil;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

@Service
@TaTransactional
public class SingleImageServiceImpl implements SingleImageService {

    private static final Log log = LogFactory.get();

    @Resource
    private FileStoreService  fileStoreService;

    @Resource
    private SingleImageStudyMapper singleImageStudyMapper;
    @Override
    public void uploadDicom(MultipartFile dicomZip) throws IOException {
        File unzippedDicomFiles = unzipDicomZip(dicomZip);
        List<File> files = FileUtil.loopFiles(unzippedDicomFiles.getAbsolutePath());

        if(files.size() != 1){
            throw new AppException("上传的不是单张文件");
        }
        File unzippedDicomFile =  files.get(0);

        AppAssert.isTrue(DicomUtil.isDicom(unzippedDicomFile),"上传的不是dicom文件");

        try{
            DicomInfo dicomInfo  = DicomUtil.readDicomInfo(unzippedDicomFile);
            System.out.println(dicomInfo);
            AppAssert.notNull(dicomInfo, "未读取到dicom文件信息");

            SingleImageStudyPO singleImageStudyPO = new SingleImageStudyPO();
            BeanUtil.copyProperties(dicomInfo, singleImageStudyPO);
            singleImageStudyPO.setUploadTime(DbKit.now());
            singleImageStudyPO.setUploadUserId(SecurityKit.currentUserId());

            String access_path = fileStoreService.upload(new ContextFSObject<>(dicomZip)).getAccessPath();
            singleImageStudyPO.setAccessPath(access_path);
            singleImageStudyMapper.insert( singleImageStudyPO);

        }finally{
            FileUtil.del(unzippedDicomFile);
        }
    }

    @Override
    public SingleImageInfoVO queryInitInfo(String studyid) {
        SingleImageStudyPO singleImageStudyPO=  singleImageStudyMapper.selectOne(Wrappers.<SingleImageStudyPO>lambdaQuery()
                .eq(SingleImageStudyPO::getId, studyid));
        SingleImageInfoVO singleImageInfoVO = new SingleImageInfoVO();
        singleImageInfoVO.setSeriesInstanceUid(singleImageStudyPO.getStudyInstanceUid());
        singleImageInfoVO.setManufacturer(singleImageStudyPO.getManufacturer());
        singleImageInfoVO.setInstitutionName(singleImageStudyPO.getInstitutionName());
        singleImageInfoVO.setPatientId(singleImageStudyPO.getPatientId());
        singleImageInfoVO.setPatientAge(singleImageStudyPO.getPatientAge());
        singleImageInfoVO.setPatientName(singleImageStudyPO.getPatientName());
        singleImageInfoVO.setPatientSex(singleImageStudyPO.getPatientSex());
        singleImageInfoVO.setStudyDateAndTime(singleImageStudyPO.getStudyDateAndTime());
        singleImageInfoVO.setPixelSpacing(singleImageStudyPO.getPixelSpacing());
        singleImageInfoVO.setKvp(singleImageStudyPO.getKvp());
        singleImageInfoVO.setSliceThickness(singleImageStudyPO.getSliceThickness());

        return singleImageInfoVO;
    }

    @Override
    public void downloadSlice(String studyId, HttpServletResponse response) {
        SingleImageStudyPO singleImageStudyPO=  singleImageStudyMapper.selectOne(Wrappers.<SingleImageStudyPO>lambdaQuery()
                .eq(SingleImageStudyPO::getId, studyId));
        AppAssert.notNull(singleImageStudyPO, "未找到影像");
        try (InputStream in = fileStoreService.download(singleImageStudyPO.getAccessPath())) {
            ResponseExportUtil.exportFileWithStream(response, in, Joiner.on(".").join(studyId, "zip"));
        } catch (IOException e) {
            log.error(e, "下载影像异常，studyId = {} ", studyId);
            throw new AppException("下载影像异常");
        }
    }


    private File unzipDicomZip(MultipartFile dicomZip) {
        // 创建临时文件用于解压DICOM ZIP文件
        File tempDir;
        try {
            tempDir = Files.createTempDirectory("dicom").toFile();
        } catch (IOException e) {
            log.error(e);
            throw new AppException("创建临时文件异常");
        }

        try (InputStream inputStream = dicomZip.getInputStream()) {
            ZipUtil.unzip(inputStream, tempDir, Charset.defaultCharset());
        } catch (IOException e) {
            FileUtil.del(tempDir);
            log.error(e);
            throw new AppException("读取DICOM文件内容异常");
        }
        return tempDir;
    }
}