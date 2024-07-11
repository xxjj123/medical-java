package com.yinhai.mids.business.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.entity.model.DicomInfo;
import com.yinhai.ta404.core.exception.AppException;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author zhuhs
 * @date 2024/7/5 15:13
 */
public class DicomUtil {

    private static final Log log = LogFactory.get();

    /**
     * 读取DICOM文件信息
     *
     * @param dicomFile DICOM文件
     * @return {@link DicomInfo }
     * @author zhuhs 2024/07/11 09:54
     */
    public static DicomInfo readDicomInfo(File dicomFile) {
        DicomInfo dicomInfo = new DicomInfo();
        try (DicomInputStream dis = new DicomInputStream(dicomFile)) {
            Attributes dataset = dis.readDataset();
            dicomInfo.setStudyInstanceUid(dataset.getString(Tag.StudyInstanceUID));
            dicomInfo.setSeriesInstanceUid(dataset.getString(Tag.SeriesInstanceUID));
            dicomInfo.setSopInstanceUid(dataset.getString(Tag.SOPInstanceUID));
            dicomInfo.setAccessionNumber(dataset.getString(Tag.AccessionNumber));
            dicomInfo.setPatientId(dataset.getString(Tag.PatientID));
            dicomInfo.setPatientName(dataset.getString(Tag.PatientName));
            dicomInfo.setPatientAge(dataset.getString(Tag.PatientAge));
            dicomInfo.setStudyDateAndTime(dataset.getDate(Tag.StudyDateAndTime));
            dicomInfo.setStudyDescription(dataset.getString(Tag.StudyDescription));
            dicomInfo.setSeriesNumber(dataset.getString(Tag.SeriesNumber));
            dicomInfo.setSeriesDescription(dataset.getString(Tag.SeriesDescription));
            dicomInfo.setInstanceNumber(Integer.valueOf(dataset.getString(Tag.InstanceNumber)));
            dicomInfo.setFile(dicomFile);
        } catch (IOException e) {
            log.error(e);
            throw new AppException("DICOM文件解析异常！");
        }
        return dicomInfo;
    }

    /**
     * 从DICOM文件夹读取DICOM信息，会去除重复的DICOM文件信息
     *
     * @param dicomDir DICOM文件夹
     * @return {@link List }<{@link DicomInfo }>
     * @author zhuhs 2024/07/11 09:54
     */
    public static List<DicomInfo> readDicomInfoFromDir(File dicomDir) {
        List<DicomInfo> dicomInfoList = new ArrayList<>();
        Set<String> sopInstanceUidSet = new HashSet<>();
        for (File dicomFile : FileUtil.ls(dicomDir.getAbsolutePath())) {
            if (FileUtil.isDirectory(dicomFile)) {
                continue;
            }
            DicomInfo dicomInfo = readDicomInfo(dicomFile);
            if (sopInstanceUidSet.contains(dicomInfo.getSopInstanceUid())) {
                continue;
            }
            sopInstanceUidSet.add(dicomInfo.getSopInstanceUid());
            dicomInfoList.add(dicomInfo);
        }
        return dicomInfoList;
    }

    public static File zipFiles(List<File> files, String zipName) {
        try {
            File tempDir = Files.createTempDirectory("dicom").toFile();
            File zipFile = new File(tempDir, zipName);
            try (ZipArchiveOutputStream zaos = new ZipArchiveOutputStream(zipFile)) {
                for (File fileToCompress : files) {
                    if (fileToCompress.exists() && !fileToCompress.isDirectory()) {
                        ZipArchiveEntry entry = new ZipArchiveEntry(fileToCompress, fileToCompress.getName());
                        zaos.putArchiveEntry(entry);
                        FileUtil.writeToStream(fileToCompress, zaos);
                        zaos.closeArchiveEntry();
                    }
                }
            }
            return zipFile;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new AppException("DICOM文件处理异常！");
        }
    }

}