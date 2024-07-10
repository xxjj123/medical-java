package com.yinhai.mids.business.util;

import cn.hutool.core.io.FileUtil;
import com.yinhai.mids.business.entity.model.DicomInfo;
import com.yinhai.ta404.core.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * @author zhuhs
 * @date 2024/7/5 15:13
 */
@Slf4j
public class DicomUtil {

    public static DicomInfo readDicomInfo(File dicomFile) {
        DicomInfo info = new DicomInfo();
        try (DicomInputStream dis = new DicomInputStream(dicomFile)) {
            Attributes dataset = dis.readDataset();
            info.setStudyUid(dataset.getString(Tag.StudyInstanceUID));
            info.setAccessionNumber(dataset.getString(Tag.AccessionNumber));
            info.setPatientId(dataset.getString(Tag.PatientID));
            info.setPatientName(dataset.getString(Tag.PatientName));
            info.setPatientAge(dataset.getString(Tag.PatientAge));
            info.setStudyDatetime(dataset.getDate(Tag.StudyDateAndTime));
            info.setStudyDescription(dataset.getString(Tag.StudyDescription));
            info.setSeriesUid(dataset.getString(Tag.SeriesInstanceUID));
            info.setSeriesNumber(dataset.getString(Tag.SeriesNumber));
            info.setSeriesDescription(dataset.getString(Tag.SeriesDescription));
            info.setInstanceUid(dataset.getString(Tag.SOPInstanceUID));
            info.setInstanceNumber(Integer.valueOf(dataset.getString(Tag.InstanceNumber)));
            info.setFile(dicomFile);
        } catch (IOException e) {
            throw new AppException("DICOM文件解析异常！");
        }
        return info;
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
