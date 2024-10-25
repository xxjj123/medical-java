package com.yinhai.mids.business.util;

import cn.hutool.core.io.FileTypeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.yinhai.mids.business.entity.model.DicomInfo;
import com.yinhai.ta404.core.exception.AppException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

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
            dicomInfo.setPatientSex(dataset.getString(Tag.PatientSex));
            dicomInfo.setPatientAge(dataset.getString(Tag.PatientAge));
            dicomInfo.setStudyDateAndTime(dataset.getDate(Tag.StudyDateAndTime));
            dicomInfo.setStudyDescription(dataset.getString(Tag.StudyDescription));
            dicomInfo.setSeriesNumber(dataset.getString(Tag.SeriesNumber));
            dicomInfo.setSeriesDescription(dataset.getString(Tag.SeriesDescription));
            dicomInfo.setInstanceNumber(Integer.valueOf(dataset.getString(Tag.InstanceNumber)));
            dicomInfo.setSliceThickness(dataset.getString(Tag.SliceThickness));
            dicomInfo.setKvp(dataset.getString(Tag.KVP));
            double[] pixelSpacing = dataset.getDoubles(Tag.PixelSpacing);
            if (ArrayUtil.isNotEmpty(pixelSpacing)) {
                dicomInfo.setPixelSpacing(Arrays.stream(pixelSpacing).mapToObj(Double::toString).collect(Collectors.joining(",")));
            }
            dicomInfo.setInstitutionName(dataset.getString(Tag.InstitutionName));
            dicomInfo.setManufacturer(dataset.getString(Tag.Manufacturer));
            double[] imagePositionPatient = dataset.getDoubles(Tag.ImagePositionPatient);
            if (ArrayUtil.isNotEmpty(imagePositionPatient)) {
                dicomInfo.setSlicePosition(imagePositionPatient[2]);
            }
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
        for (File dicomFile : FileUtil.loopFiles(dicomDir.getAbsolutePath())) {
            if (FileUtil.isDirectory(dicomFile)) {
                continue;
            }
            if (!isDicom(dicomFile)) {
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

    private static final String DICOM_HEADER = "DICM";

    public static boolean isDicom(File file) {
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(new byte[128]);
            byte[] header = new byte[4];
            fis.read(header);
            return DICOM_HEADER.equals(new String(header));
        } catch (Exception e) {
            log.error(e);
            throw new AppException("DICOM文件解析异常！");
        }
    }

    public static boolean isDicom(InputStream inputStream) {
        try {
            byte[] header = IoUtil.readBytes(inputStream, 132);
            if (header.length < 132) {
                return false;
            }
            return DICOM_HEADER.equals(new String(ArrayUtil.sub(header, 128, 132)));
        } catch (Exception e) {
            log.error(e);
            throw new AppException("DICOM文件解析异常！");
        } finally {
            IoUtil.close(inputStream);
        }
    }

    public static boolean isZip(InputStream inputStream) {
        try {
            return "zip".equals(FileTypeUtil.getType(inputStream));
        } finally {
            IoUtil.close(inputStream);
        }
    }
}
