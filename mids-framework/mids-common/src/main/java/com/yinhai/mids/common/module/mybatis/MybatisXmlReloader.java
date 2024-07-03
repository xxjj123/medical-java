package com.yinhai.mids.common.module.mybatis;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用于springboot maven项目下热加载mybatis的mapper xml文件。默认不开启，需要在yml中配置xml-reload-enabled: true开启。生产环境务必记得关闭。
 * 使用方式：对mapper xml修改并保存（例如修改后通过ctrl + s保存或者鼠标点击IDEA界面之外的地方）。热加载信息可通过控制台日志查看。
 *
 * @author zhuhs
 * @version 2.1.0
 * @date 2023/10/27 16:07
 */
@Component
public class MybatisXmlReloader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MybatisXmlReloader.class);

    public static final String TARGET_CLASSES = "\\target\\classes";

    public static final String SRC = "\\src";

    /**
     * 文件修改监听间隔（毫秒）
     */
    private static final long INTERVAL = 2000;

    private final List<SqlSessionFactory> sqlSessionFactoryList;

    @Value("${xml-reload-enabled: false}")
    private boolean xmlReloadEnabled;

    public MybatisXmlReloader(List<SqlSessionFactory> sqlSessionFactoryList) {
        this.sqlSessionFactoryList = sqlSessionFactoryList;
    }

    @SuppressWarnings("unchecked")
    public void init() throws IllegalAccessException, IOException {
        log.info("MybatisXmlReloader: init start");
        List<MapperXml> targetMapperXmlList = new ArrayList<>();
        Set<Path> rootPaths = new HashSet<>();
        for (SqlSessionFactory sqlSessionFactory : sqlSessionFactoryList) {
            Object loadedResourcesObj = getFieldValue(sqlSessionFactory.getConfiguration(), "loadedResources");
            if (loadedResourcesObj == null) {
                continue;
            }
            Set<String> loadedResources = (Set<String>) loadedResourcesObj;
            for (String loadedResource : loadedResources) {
                if (!loadedResource.startsWith("file")) {
                    continue;
                }
                String absolutePath = loadedResource.substring("file [".length(), loadedResource.length() - 1);
                MapperXml mapperXml = new MapperXml(sqlSessionFactory, absolutePath,
                        loadedResource.substring(
                                loadedResource.indexOf(TARGET_CLASSES) + TARGET_CLASSES.length() + 1,
                                loadedResource.length() - 1
                        )
                );
                targetMapperXmlList.add(mapperXml);
                String srcDir = absolutePath.substring(0, absolutePath.indexOf(TARGET_CLASSES)) + SRC;
                rootPaths.add(new File(srcDir).toPath());
            }
        }
        List<MapperXml> sortedTargetMapperXmlList = targetMapperXmlList.stream()
                .sorted((o1, o2) -> o2.suffix.length() - o1.suffix.length())
                .collect(Collectors.toList());
        List<MapperXml> srcMapperXmlList = new ArrayList<>();
        List<Path> srcMapperMapperXmlPathList = new ArrayList<>();
        for (Path rootPath : rootPaths) {
            Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    String absolutePath = file.toAbsolutePath().toString();
                    String suffix = null;
                    for (MapperXml targetMapperXml : sortedTargetMapperXmlList) {
                        if (suffix == null && !absolutePath.endsWith(targetMapperXml.suffix)) {
                            continue;
                        }
                        if (suffix != null && !suffix.equals(targetMapperXml.suffix)) {
                            continue;
                        }
                        srcMapperXmlList.add(new MapperXml(targetMapperXml.sqlSessionFactory,
                                absolutePath,
                                targetMapperXml.suffix)
                        );
                        srcMapperMapperXmlPathList.add(file);
                        suffix = targetMapperXml.suffix;
                    }
                    return super.visitFile(file, attrs);
                }
            });
        }
        ThreadFactory threadFactory = r -> {
            Thread thread = new Thread(r);
            thread.setName("xml-reload");
            thread.setDaemon(true);
            return thread;
        };
        Executors.newSingleThreadScheduledExecutor(threadFactory).scheduleAtFixedRate(
                new FileChangeMonitor(new XmlChangeListener(srcMapperXmlList), srcMapperMapperXmlPathList),
                2000, INTERVAL, TimeUnit.MILLISECONDS
        );
        log.info("MybatisXmlReloader: init end");
    }

    public static Object getFieldValue(Object o, String field) throws IllegalAccessException {
        Field declaredField = null;
        Class<?> clazz = o.getClass();
        while (clazz != Object.class) {
            try {
                declaredField = clazz.getDeclaredField(field);
                break;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        if (declaredField == null) {
            throw new IllegalAccessException("parse mybatis configuration error");
        }
        declaredField.setAccessible(true);
        return declaredField.get(o);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!xmlReloadEnabled) {
            log.warn("MybatisXmlReloader not enabled, config [xml-reload-enabled: true] in your yml to enable it");
            return;
        }
        init();
    }

    private static class FileChangeMonitor implements Runnable {
        private final List<Path> pathList;

        private final XmlChangeListener xmlChangeListener;

        private final Map<Path, FileTime> pathModifiedTimeMap = new HashMap<>();

        private FileChangeMonitor(XmlChangeListener xmlChangeListener, List<Path> pathList) {
            this.xmlChangeListener = xmlChangeListener;
            this.pathList = pathList;
        }

        @Override
        public void run() {
            for (Path path : pathList) {
                FileTime lastModifiedTime;
                try {
                    lastModifiedTime = Files.getLastModifiedTime(path);
                } catch (IOException e) {
                    log.error("error happens when get {} LastModifiedTime", path.toAbsolutePath());
                    log.error(e.getMessage(), e);
                    continue;
                }
                FileTime recordModifiedTime = pathModifiedTimeMap.get(path);
                if (recordModifiedTime != null && lastModifiedTime.compareTo(recordModifiedTime) > 0) {
                    xmlChangeListener.onChange(path);
                }
                pathModifiedTimeMap.put(path, lastModifiedTime);
            }
        }
    }

    /**
     * @author zhuhs
     * @date 2023/10/27 14:53
     */
    private static class XmlChangeListener {

        private final List<MapperXml> mapperXmlList;

        public XmlChangeListener(List<MapperXml> mapperXmlList) {
            this.mapperXmlList = mapperXmlList;
        }

        public void onChange(Path path) {
            String absolutePath = path.toAbsolutePath().toString();
            for (MapperXml mapperXml : mapperXmlList) {
                if (mapperXml.absolutePath.equals(absolutePath)) {
                    try {
                        log.warn("{} start reload", absolutePath);
                        doReload(mapperXml);
                        log.warn("{} reload success", absolutePath);
                    } catch (Exception e) {
                        log.error("Failed to parse mapping resource: {}", absolutePath);
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }

        private void doReload(MapperXml mapperXml) throws IllegalAccessException, IOException {
            Configuration targetConfiguration = mapperXml.sqlSessionFactory.getConfiguration();
            FileSystemResource mapperResource = new FileSystemResource(mapperXml.absolutePath);
            removePreLoad(mapperResource, targetConfiguration);
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(
                    mapperResource.getInputStream(),
                    targetConfiguration,
                    mapperResource.toString(),
                    targetConfiguration.getSqlFragments()
            );
            xmlMapperBuilder.parse();
        }

        @SuppressWarnings("unchecked")
        private void removePreLoad(Resource mapperResource, Configuration targetConfiguration) throws IOException, IllegalAccessException {
            Set<String> loadedResources
                    = (Set<String>) getFieldValue(targetConfiguration, "loadedResources");
            // 允许重新加载
            loadedResources.clear();
            Map<String, ResultMap> resultMaps
                    = (Map<String, ResultMap>) getFieldValue(targetConfiguration, "resultMaps");
            Map<String, XNode> sqlFragmentsMaps
                    = (Map<String, XNode>) getFieldValue(targetConfiguration, "sqlFragments");
            Map<String, MappedStatement> mappedStatementMaps
                    = (Map<String, MappedStatement>) getFieldValue(targetConfiguration, "mappedStatements");
            XPathParser parser = new XPathParser(
                    mapperResource.getInputStream(),
                    true,
                    targetConfiguration.getVariables(),
                    new XMLMapperEntityResolver()
            );
            XNode mapperXnode = parser.evalNode("/mapper");
            List<XNode> resultMapNodes = mapperXnode.evalNodes("/mapper/resultMap");
            String namespace = mapperXnode.getStringAttribute("namespace");
            for (XNode xNode : resultMapNodes) {
                String id = xNode.getStringAttribute("id", xNode.getValueBasedIdentifier());
                Iterator<Map.Entry<String, ResultMap>> iterator = resultMaps.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, ResultMap> next = iterator.next();
                    String key = next.getKey();
                    if (key.contains(namespace + "." + id)) {
                        iterator.remove();
                    }
                    // 处理 association、collection标签
                    if (key.contains(namespace + ".mapper_resultMap")) {
                        iterator.remove();
                    }
                }
            }

            List<XNode> sqlNodes = mapperXnode.evalNodes("/mapper/sql");
            for (XNode sqlNode : sqlNodes) {
                String id = sqlNode.getStringAttribute("id", sqlNode.getValueBasedIdentifier());
                sqlFragmentsMaps.remove(namespace + "." + id);
            }

            List<XNode> msNodes = mapperXnode.evalNodes("select|insert|update|delete");
            for (XNode msNode : msNodes) {
                String id = msNode.getStringAttribute("id", msNode.getValueBasedIdentifier());
                mappedStatementMaps.remove(namespace + "." + id);
            }
        }
    }

    private static class MapperXml {
        private final SqlSessionFactory sqlSessionFactory;
        private final String absolutePath;
        private final String suffix;

        public MapperXml(SqlSessionFactory sqlSessionFactory, String absolutePath, String suffix) {
            this.sqlSessionFactory = sqlSessionFactory;
            this.absolutePath = absolutePath;
            this.suffix = suffix;
        }
    }
}
