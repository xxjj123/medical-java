package com.yinhai.mids.business.util;

import com.yinhai.mids.business.entity.model.DicomInfo;
import com.yinhai.ta404.core.exception.AppException;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.Tag;
import org.dcm4che3.io.DicomInputStream;

import java.io.File;
import java.io.IOException;

/**
 * @author zhuhs
 * @date 2024/7/5 15:13
 */
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
        } catch (IOException e) {
            throw new AppException("DICOM文件解析异常！");
        }
        return info;
    }

}
