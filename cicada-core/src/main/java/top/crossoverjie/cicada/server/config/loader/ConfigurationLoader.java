package top.crossoverjie.cicada.server.config.loader;

import org.slf4j.Logger;
import top.crossoverjie.cicada.base.log.LoggerBuilder;
import top.crossoverjie.cicada.base.configuration.AbstractCicadaConfiguration;
import top.crossoverjie.cicada.server.config.holder.ConfigurationHolder;
import top.crossoverjie.cicada.server.exception.GenericException;
import top.crossoverjie.cicada.server.scanner.ClasLoadersScanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * 配置加载器
 * 职责：
 * 1. 扫描配置类
 * 2. 加载配置文件
 * 3. 初始化配置对象
 */
public class ConfigurationLoader {
    
    private static final Logger LOGGER = LoggerBuilder.getLogger(ConfigurationLoader.class);

    /**
     * 加载所有配置
     * @param rootPackage 根包名
     */
    public static void loadAll(String rootPackage) throws Exception {
        // 1. 扫描配置类
        List<Class<?>> configClasses = ClasLoadersScanner.getConfiguration(rootPackage);
        
        // 2. 加载每个配置类
        for (Class<?> configClass : configClasses) {
            loadConfiguration(configClass);
        }
    }

    /**
     * 加载单个配置类
     */
    private static void loadConfiguration(Class<?> configClass) throws Exception {
        AbstractCicadaConfiguration config = (AbstractCicadaConfiguration) configClass.newInstance();
        
        // 1. 获取配置文件输入流
        InputStream stream = getConfigStream(config.getPropertiesName());
        
        // 2. 加载配置属性
        Properties properties = new Properties();
        properties.load(stream);
        config.setProperties(properties);
        
        // 3. 注册到配置持有器
        ConfigurationHolder.addConfiguration(configClass.getName(), config);
        
        LOGGER.info("Loaded configuration: {}", configClass.getName());
    }

    /**
     * 获取配置文件输入流
     * 优先级：系统属性 > 类路径
     */
    private static InputStream getConfigStream(String propertiesName) throws Exception {
        // 1. 尝试从系统属性获取
        String systemPath = System.getProperty(propertiesName);
        if (systemPath != null) {
            File file = new File(systemPath);
            if (file.exists()) {
                return new FileInputStream(file);
            }
            throw new GenericException("Configuration file not found: " + systemPath);
        }
        
        // 2. 从类路径获取
        InputStream stream = ConfigurationLoader.class.getClassLoader()
                .getResourceAsStream(propertiesName);
        if (stream != null) {
            return stream;
        }
        
        throw new GenericException("Configuration file not found in classpath: " + propertiesName);
    }
}
