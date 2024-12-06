package com.yinhai.mids.common.module.logback;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import com.yinhai.ta404.core.log.logback.config.AbstractAppenderLoader;
import com.yinhai.ta404.core.log.logback.converter.NodeIpConverter;
import com.yinhai.ta404.core.utils.ValidateUtil;

import java.util.Map;

/**
 * @author zhuhs
 * @date 2024/12/6
 */
public class RollingFileAppenderLoader extends AbstractAppenderLoader {
    private static final String DEFAULT_FILE_MAX_SIZE = "10mb";
    private static final String DEFAULT_FILE_TOTAL_SIZE = "10gb";
    private static final int DEFAULT_MAX_HISTORY = 30;
    private static final String DEFAULT_FILE_FORMAT = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} : %msg%n";
    private static final String FILE_NAME_PATTERN = "file-name-pattern";
    private static final String MAX_FILE_SIZE = "max-file-Size";
    private static final String MAX_HISTORY = "max-history";
    private static final String CLEAR_SIZE = "clear-size";
    private static final String FORMAT = "format";
    private static final String FILTER_LEVEL = "filter-level";
    private static final String ASYNC = "async";
    private static final String APPENDER_ASYNC_FILE = "async_file";
    private static final int FILE_ASYNC_QUEUE_SIZE_DEFAULT = 512;
    private static final String FILE_ASYNC_QUEUE_SIZE = "async-queue-size";
    private static final int FILE_ASYNC_DISCARDING_THRESHOLD_DEFAULT = 0;
    private static final String FILE_ASYNC_DISCARDING_THRESHOLD = "async-discarding-threshold";
    private static final boolean FILE_ASYNC_NEVER_BLOCK_DEFAULT = false;
    private static final String FILE_ASYNC_NEVER_BLOCK = "async-never-block";

    public RollingFileAppenderLoader() {
    }

    @Override
    public Appender<ILoggingEvent> loadAppender(Map<String, String> fileMap, LoggerContext loggerContext) {
        if (fileMap != null && !fileMap.isEmpty()) {
            RollingFileAppender<ILoggingEvent> ra = this.configRollingFileAppender(fileMap, loggerContext);
            String async = fileMap.get("async");
            boolean isAsync;
            if (ValidateUtil.isEmpty(async)) {
                isAsync = false;
            } else {
                isAsync = Boolean.parseBoolean(async);
            }

            return isAsync ? this.configAsyncAppender(fileMap, loggerContext, ra) : ra;
        } else {
            return null;
        }
    }

    private SizeAndTimeBasedRollingPolicy<Object> configSizeAndTimeBasedRollingPolicy(Map<String, String> fileMap, RollingFileAppender<ILoggingEvent> rfa, LoggerContext loggerContext) {
        SizeAndTimeBasedRollingPolicy<Object> rollingPolicy = new SizeAndTimeBasedRollingPolicy<>();
        rollingPolicy.setContext(loggerContext);
        String fileNamePattern = fileMap.get("file-name-pattern");
        if (ValidateUtil.isEmpty(fileNamePattern)) {
            throw new IllegalArgumentException("未配置[application.yml]文件的[ta404.log.appender-type.file.file-name-pattern]属性");
        } else {
            if (fileNamePattern.contains("%node_ip")) {
                NodeIpConverter nodeIpConverter = new NodeIpConverter();
                nodeIpConverter.setContext(loggerContext);
                fileNamePattern = fileNamePattern.replaceAll("%node_ip", nodeIpConverter.convert(null));
            }

            rollingPolicy.setFileNamePattern(fileNamePattern);
            String maxFileSize = fileMap.get("max-file-Size");
            if (ValidateUtil.isEmpty(maxFileSize)) {
                maxFileSize = "10mb";
            }

            rollingPolicy.setMaxFileSize(FileSize.valueOf(maxFileSize));
            String maxHistory = fileMap.get("max-history");
            if (ValidateUtil.isEmpty(maxHistory)) {
                rollingPolicy.setMaxHistory(30);
            } else {
                rollingPolicy.setMaxHistory(Integer.parseInt(maxHistory));
            }

            String clearSize = fileMap.get("clear-size");
            if (ValidateUtil.isEmpty(clearSize)) {
                clearSize = "10gb";
            }

            rollingPolicy.setTotalSizeCap(FileSize.valueOf(clearSize));
            rollingPolicy.setParent(rfa);
            rollingPolicy.start();
            return rollingPolicy;
        }
    }

    private RollingFileAppender<ILoggingEvent> configRollingFileAppender(Map<String, String> fileMap, LoggerContext loggerContext) {
        RollingFileAppender<ILoggingEvent> rfa = new RollingFileAppender<>();
        rfa.setContext(loggerContext);
        rfa.setName("file");
        // 重写框架开始
        String file = fileMap.get("file");
        if (ValidateUtil.isNotEmpty(file)) {
            rfa.setFile(file);
        }
        // 重写框架结束
        String fileFormat = fileMap.get("format");
        if (ValidateUtil.isEmpty(fileFormat)) {
            fileFormat = "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} : %msg%n";
        }

        String filterLevel = fileMap.get("filter-level");
        if (ValidateUtil.isNotEmpty(filterLevel)) {
            rfa.addFilter(this.configFilter(filterLevel, loggerContext));
        }

        rfa.setEncoder(this.configEncoder(fileFormat, loggerContext));
        rfa.setRollingPolicy(this.configSizeAndTimeBasedRollingPolicy(fileMap, rfa, loggerContext));
        rfa.start();
        return rfa;
    }

    private AsyncAppender configAsyncAppender(Map<String, String> fileMap, LoggerContext loggerContext, RollingFileAppender<ILoggingEvent> ka) {
        AsyncAppender aa = new AsyncAppender();
        aa.setContext(loggerContext);
        aa.setName("async_file");
        aa.setDiscardingThreshold(ValidateUtil.isEmpty(fileMap.get("async-discarding-threshold")) ? 0 : Integer.parseInt(fileMap.get("async-discarding-threshold")));
        aa.setQueueSize(ValidateUtil.isEmpty(fileMap.get("async-queue-size")) ? 512 : Integer.parseInt(fileMap.get("async-queue-size")));
        aa.setNeverBlock(!ValidateUtil.isEmpty(fileMap.get("async-never-block")) && Boolean.parseBoolean(fileMap.get("async-never-block")));
        aa.addAppender(ka);
        aa.start();
        return aa;
    }
}
