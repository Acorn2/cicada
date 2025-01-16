package top.crossoverjie.cicada.server.scanner;

import org.slf4j.Logger;
import top.crossoverjie.cicada.base.bean.CicadaBeanFactory;
import top.crossoverjie.cicada.base.log.LoggerBuilder;
import top.crossoverjie.cicada.base.annotation.CicadaAction;
import top.crossoverjie.cicada.base.annotation.CicadaBean;
import top.crossoverjie.cicada.base.annotation.CicadaInterceptor;
import top.crossoverjie.cicada.server.bean.CicadaDefaultBean;
import top.crossoverjie.cicada.server.lifecycle.InitializeHandle;
import top.crossoverjie.cicada.base.configuration.AbstractCicadaConfiguration;
import top.crossoverjie.cicada.server.config.configuration.ApplicationConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 类扫描器：负责扫描和加载指定包下的类
 * 主要功能：
 * 1. 扫描指定包下的所有类
 * 2. 加载特定注解标注的类
 * 3. 支持从文件系统和jar包中加载类
 */
public final class ClasLoadersScanner {

    private final static Logger LOGGER = LoggerBuilder.getLogger(ClasLoadersScanner.class);

    // 缓存已扫描的Action和Bean映射
    private static Map<String, Class<?>> actionMap = null;
    // 缓存已扫描的拦截器
    private static Map<Class<?>, Integer> interceptorMap = null;
    // 缓存所有扫描到的类
    private static Set<Class<?>> classes = null;
    // 缓存框架相关的类
    private static Set<Class<?>> cicada_classes = null;
    // 缓存配置类列表
    private static List<Class<?>> configurationList = null;
    // 缓存初始化处理器列表
    private static List<Class<?>> initHandleList = null;

    /**
     * 获取配置类
     * @param packageName 包名
     * @return 配置类列表
     */
    public static List<Class<?>> getConfiguration(String packageName) throws Exception {
        if (configurationList == null) {
            Set<Class<?>> clsList = getClasses(packageName);
            // 手动添加ApplicationConfiguration
            clsList.add(ApplicationConfiguration.class);
            
            if (clsList == null || clsList.isEmpty()) {
                return configurationList;
            }

            configurationList = new ArrayList<>(16);
            // 筛选继承自AbstractCicadaConfiguration的类
            for (Class<?> cls : clsList) {
                if (cls.getSuperclass() != AbstractCicadaConfiguration.class) {
                    continue;
                }
                configurationList.add(cls);
            }
        }
        return configurationList;
    }

    /**
     * 获取初始化处理器类
     * @param packageName 包名
     * @return 初始化处理器列表
     */
    public static List<Class<?>> getInitHandles(String packageName) throws Exception {
        if (initHandleList == null){
            Set<Class<?>> clsList = getClasses(packageName);

            if (clsList == null || clsList.isEmpty()) {
                return initHandleList;
            }

            initHandleList = new ArrayList<>(16);
            for (Class<?> cls : clsList) {

                if (cls.getSuperclass() != InitializeHandle.class) {
                    continue;
                }
                initHandleList.add(cls) ;
            }
        }
        return initHandleList ;
    }

    /**
     * 获取带有@CicadaAction和@CicadaBean注解的类
     * @param packageName 包名
     * @return Action和Bean的映射关系
     */
    public static Map<String, Class<?>> getCicadaBean(String packageName) throws Exception {
        if (actionMap == null) {
            Set<Class<?>> clsList = getClasses(packageName);
            
            if (clsList == null || clsList.isEmpty()) {
                return actionMap;
            }

            actionMap = new HashMap<>(16);
            // 扫描带有特定注解的类
            for (Class<?> cls : clsList) {
                CicadaAction action = cls.getAnnotation(CicadaAction.class);
                CicadaBean bean = cls.getAnnotation(CicadaBean.class);
                if (action == null && bean == null) {
                    continue;
                }

                // 注册Action
                if (action != null) {
                    actionMap.put(action.value() == null ? cls.getName() : action.value(), cls);
                }
                // 注册Bean
                if (bean != null) {
                    actionMap.put(bean.value() == null ? cls.getName() : bean.value(), cls);
                }
            }
        }
        return actionMap;
    }

    /**
     * whether is the target class
     * @param clazz
     * @param target
     * @return
     */
    public static boolean isInterface(Class<?> clazz,Class<?> target){
        for (Class<?> aClass : clazz.getInterfaces()) {
            if (aClass.getName().equals(target.getName())){
                return true ;
            }
        }
        return false ;
    }

    /**
     * 获取带有@Interceptor注解的类
     * @param packageName 包名
     * @return 拦截器类和优先级的映射
     */
    public static Map<Class<?>, Integer> getCicadaInterceptor(String packageName) throws Exception {
        if (interceptorMap == null) {
            Set<Class<?>> clsList = getClasses(packageName);

            if (clsList == null || clsList.isEmpty()) {
                return interceptorMap;
            }

            interceptorMap = new HashMap<>(16);
            for (Class<?> cls : clsList) {
                Annotation annotation = cls.getAnnotation(CicadaInterceptor.class);
                if (annotation == null) {
                    continue;
                }

                CicadaInterceptor interceptor = (CicadaInterceptor) annotation;
                interceptorMap.put(cls, interceptor.order());

            }
        }

        return interceptorMap;
    }

    /**
     * 扫描指定包下的所有类
     * @param packageName 包名
     * @return 类集合
     */
    public static Set<Class<?>> getClasses(String packageName) throws Exception {
        if (classes == null) {
            classes = new HashSet<>(32);
            baseScanner(packageName, classes);
        }
        return classes;
    }

    /**
     * 基础扫描器：支持从文件系统和jar包中扫描类
     * @param packageName 包名
     * @param set 存储扫描结果的集合
     */
    private static void baseScanner(String packageName, Set set) {
        boolean recursive = true;

        String packageDirName = packageName.replace('.', '/');

        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    findAndAddClassesInPackageByFile(packageName, filePath, recursive, set);
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        Enumeration<JarEntry> entries = jar.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.charAt(0) == '/') {
                                name = name.substring(1);
                            }
                            if (name.startsWith(packageDirName)) {
                                int idx = name.lastIndexOf('/');
                                if (idx != -1) {
                                    packageName = name.substring(0, idx).replace('/', '.');
                                }
                                if ((idx != -1) || recursive) {
                                    if (name.endsWith(".class") && !entry.isDirectory()) {
                                        String className = name.substring(packageName.length() + 1, name.length() - 6);
                                        try {
                                            set.add(Class.forName(packageName + '.' + className));
                                        } catch (ClassNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        LOGGER.error("IOException", e);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("IOException", e);
        }
    }


    private static void findAndAddClassesInPackageByFile(String packageName,
                                                        String packagePath, final boolean recursive, Set<Class<?>> classes) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        File[] files = dir.listFiles(file -> (recursive && file.isDirectory())
                || (file.getName().endsWith(".class")));
        for (File file : files) {
            if (file.isDirectory()) {
                findAndAddClassesInPackageByFile(packageName + "."
                                + file.getName(), file.getAbsolutePath(), recursive,
                        classes);
            } else {
                String className = file.getName().substring(0,
                        file.getName().length() - 6);
                try {
                    classes.add(Thread.currentThread().getContextClassLoader().loadClass(packageName + '.' + className));
                } catch (ClassNotFoundException e) {
                    LOGGER.error("ClassNotFoundException", e);
                }
            }
        }
    }


    /**
     * 通过SPI机制获取CicadaBeanFactory实现
     * @return CicadaBeanFactory实例
     */
    public static CicadaBeanFactory getCicadaBeanFactory() {
        // 加载SPI配置的实现类
        ServiceLoader<CicadaBeanFactory> cicadaBeanFactories = ServiceLoader.load(CicadaBeanFactory.class);
        if (cicadaBeanFactories.iterator().hasNext()) {
            return cicadaBeanFactories.iterator().next();
        }
        // 默认使用CicadaDefaultBean
        return new CicadaDefaultBean();
    }

}
