package com.yinhai.mids.business.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ZipUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.dtflys.forest.http.ForestResponse;
import com.google.common.base.Joiner;
import com.yinhai.mids.business.entity.model.ContextFSObject;
import com.yinhai.mids.business.entity.model.DicomInfo;
import com.yinhai.mids.business.entity.po.SingleImageStudyPO;
import com.yinhai.mids.business.entity.vo.SingleImageInfoVO;
import com.yinhai.mids.business.entity.vo.SpineInfoVO;
import com.yinhai.mids.business.mapper.SingleImageStudyMapper;
import com.yinhai.mids.business.service.FileStoreService;
import com.yinhai.mids.business.service.SingleImageService;
import com.yinhai.mids.business.spine.SpineClient;
import com.yinhai.mids.business.spine.SpineProperties;
import com.yinhai.mids.business.spine.SpineResponse;
import com.yinhai.mids.business.util.DicomUtil;
import com.yinhai.mids.common.exception.AppAssert;
import com.yinhai.mids.common.util.DbClock;
import com.yinhai.mids.common.util.SecurityKit;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.core.transaction.annotation.TaTransactional;
import com.yinhai.ta404.core.utils.ResponseExportUtil;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReadParam;
import org.dcm4che3.imageio.plugins.dcm.DicomImageReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

@Service
@TaTransactional
public class SingleImageServiceImpl implements SingleImageService {

    private static final Log log = LogFactory.get();

    @Resource
    private FileStoreService fileStoreService;

    @Resource
    private SingleImageStudyMapper singleImageStudyMapper;

    @Resource
    private SpineClient spineClient;

    @Resource
    private SpineProperties spineProperties;

    @Override
    public void uploadDicom(MultipartFile dicomZip) throws IOException {
        File unzippedDicomFiles = unzipDicomZip(dicomZip);
        List<File> files = FileUtil.loopFiles(unzippedDicomFiles.getAbsolutePath());

        if (files.size() != 1) {
            throw new AppException("上传的不是单张文件");
        }
        File unzippedDicomFile = files.get(0);

        AppAssert.isTrue(DicomUtil.isDicom(unzippedDicomFile), "上传的不是dicom文件");

        try {
            DicomInfo dicomInfo = DicomUtil.readDicomInfo(unzippedDicomFile);
            System.out.println(dicomInfo);
            AppAssert.notNull(dicomInfo, "未读取到dicom文件信息");

            SingleImageStudyPO singleImageStudyPO = new SingleImageStudyPO();
            BeanUtil.copyProperties(dicomInfo, singleImageStudyPO);
            singleImageStudyPO.setUploadTime(DbClock.now());
            singleImageStudyPO.setUploadUserId(SecurityKit.currentUserId());

            String access_path = fileStoreService.upload(new ContextFSObject<>(dicomZip)).getAccessPath();
            singleImageStudyPO.setAccessPath(access_path);
            singleImageStudyMapper.insert(singleImageStudyPO);

        } finally {
            FileUtil.del(unzippedDicomFile);
        }
    }

    @Override
    public SingleImageInfoVO queryInitInfo(String studyid) {
        SingleImageStudyPO singleImageStudyPO = singleImageStudyMapper.selectOne(Wrappers.<SingleImageStudyPO>lambdaQuery()
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
        SingleImageStudyPO singleImageStudyPO = singleImageStudyMapper.selectOne(Wrappers.<SingleImageStudyPO>lambdaQuery()
                .eq(SingleImageStudyPO::getId, studyId));
        AppAssert.notNull(singleImageStudyPO, "未找到影像");
        try (InputStream in = fileStoreService.download(singleImageStudyPO.getAccessPath())) {
            ResponseExportUtil.exportFileWithStream(response, in, Joiner.on(".").join(studyId, "zip"));
        } catch (IOException e) {
            log.error(e, "下载影像异常，studyId = {} ", studyId);
            throw new AppException("下载影像异常");
        }
    }

    @Override
    public SpineInfoVO getSpineInfo(MultipartFile dicomZip) throws IOException {

        File unzippedDicomFiles = unzipDicomZip(dicomZip);
        List<File> files = FileUtil.loopFiles(unzippedDicomFiles.getAbsolutePath());

        if (files.size() != 1) {
            throw new AppException("上传的不是单张文件");
        }
        File file = files.get(0);
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("DICOM");
        DicomImageReader reader=(DicomImageReader)readers.next();
        reader.setInput(ImageIO.createImageInputStream(file));

        BufferedImage image = reader.read(0, new DicomImageReadParam());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);  // 将图像写入 ByteArrayOutputStream
        byte[] bytes = baos.toByteArray();  // 转换为字节数组
        baos.close();
        String base64 = Base64.getEncoder().encodeToString(bytes);

        ForestResponse<SpineResponse> resp = spineClient.getBoneInfo(spineProperties.getQueryUrl(),base64 );
        if (resp.isError()) {
            log.error("连接AI服务失败，请检查网络配置或AI服务是否正常");
        }
        SpineResponse spineResponse = resp.getResult();
        SpineInfoVO spineInfoVO = new SpineInfoVO();
        spineInfoVO.setData(spineResponse.getTemplate());
        return spineInfoVO;
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
