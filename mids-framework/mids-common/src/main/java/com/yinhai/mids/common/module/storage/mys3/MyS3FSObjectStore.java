package com.yinhai.mids.common.module.storage.mys3;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReflectUtil;
import cn.hutool.core.util.StrUtil;
import com.yinhai.ta404.core.exception.AppException;
import com.yinhai.ta404.module.storage.ta.all.object.AbstractFSObjectStore;
import com.yinhai.ta404.module.storage.ta.all.object.impl.S3FSObjectStore;
import com.yinhai.ta404.module.storage.ta.all.properties.FSBusinessTypeAndBucket;
import com.yinhai.ta404.module.storage.ta.all.properties.S3StorageProperties;
import com.yinhai.ta404.storage.ta.core.FSObject;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 包装框架S3实现，修改key生成规则以及自定义metadata设置
 *
 * @author zhuhs
 * @date 2024/6/28 9:30
 */
@Slf4j
public class MyS3FSObjectStore extends AbstractFSObjectStore {

    private static final Logger logger = LoggerFactory.getLogger(S3FSObjectStore.class);

    private final S3Client s3;

    private final String bucketName;

    private final S3FSObjectStore delegate;

    public MyS3FSObjectStore(S3StorageProperties s3StorageProperties, FSBusinessTypeAndBucket fsBusinessTypeAndBucket) {
        this.delegate = new S3FSObjectStore(s3StorageProperties, fsBusinessTypeAndBucket);
        this.s3 = (S3Client) ReflectUtil.getFieldValue(S3FSObjectStore.class, "s3");
        this.bucketName = (String) ReflectUtil.getFieldValue(delegate, "bucketName");
    }


    @Override
    public FSObject putObject(FSObject fs) {
        InputStream inputStream = fs.getInputstream();
        if (inputStream != null && fs.getName() != null) {
            // 注意这里不能使用大写，因为s3会转为自动小写，导致取不出来
            Map<String, String> map = new HashMap<>();
            map.put("name", fs.getName());
            map.put("content-type", fs.getContentType());
            String dateString = S3FSObjectStore.dateToStrLong(new Date());
            map.put("updatedate", dateString);
            String keyId;
            if (StrUtil.isNotBlank(fs.getKeyId())) {
                keyId = fs.getKeyId();
            } else {
                keyId = DateUtil.format(new Date(), "yyyyMMdd") + "/" + IdUtil.fastSimpleUUID();
                fs.setKeyId(keyId);
            }
            try {
                s3.putObject(
                        PutObjectRequest.builder().bucket(this.bucketName).key(keyId).metadata(map).build(),
                        RequestBody.fromInputStream(inputStream, fs.getSize())
                );
            } catch (Exception e) {
                throw new AppException(e);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("资源关闭异常", e);
                }
            }
            return fs;
        } else {
            throw new AppException(fs.getKeyId() + "文件不能为空！");
        }
    }

    @Override
    public FSObject[] batchPutObject(FSObject[] fss) {
        FSObject[] ret = new FSObject[fss.length];
        for (int i = 0; i < fss.length; ++i) {
            FSObject fs = fss[i];
            ret[i] = putObject(fs);
        }
        return ret;
    }

    @Override
    public FSObject getObject(String keyId) {
        try {
            ResponseBytes<GetObjectResponse> rsp = s3.getObject(
                    GetObjectRequest.builder().bucket(this.bucketName).key(keyId).build(),
                    ResponseTransformer.toBytes()
            );
            int size = rsp.response().contentLength().intValue();
            String name = rsp.response().metadata().get("name");
            String contentType = rsp.response().metadata().get("content-type");
            String dateString = rsp.response().metadata().get("updatedate");
            Date updatedate = S3FSObjectStore.strToDateLong(dateString);
            FSObject fsobject = new FSObject();
            fsobject.setKeyId(keyId);
            fsobject.setName(name);
            fsobject.setSize(size);
            fsobject.setContentType(contentType);
            fsobject.setUpdatedate(updatedate);
            fsobject.setInputstream(rsp.asInputStream());
            return fsobject;
        } catch (Exception e) {
            throw new AppException(e);
        }
    }

    @Override
    public boolean deleteObject(String keyId) {
        return delegate.deleteObject(keyId);
    }
}
