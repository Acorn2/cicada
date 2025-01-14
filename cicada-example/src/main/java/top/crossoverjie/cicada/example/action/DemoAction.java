package top.crossoverjie.cicada.example.action;

import org.slf4j.Logger;
import top.crossoverjie.cicada.base.log.LoggerBuilder;
import top.crossoverjie.cicada.example.configuration.KafkaConfiguration;
import top.crossoverjie.cicada.example.configuration.RedisConfiguration;
import top.crossoverjie.cicada.server.configuration.ApplicationConfiguration;
import top.crossoverjie.cicada.server.configuration.ConfigurationHolder;

import static top.crossoverjie.cicada.server.configuration.ConfigurationHolder.getConfiguration;

/**
 * Function:
 *
 * @author crossoverJie
 *         Date: 2018/8/31 18:52
 * @since JDK 1.8
 */
public class DemoAction {


    private static final Logger LOGGER = LoggerBuilder.getLogger(DemoAction.class);

    public void execute() {

        KafkaConfiguration configuration = (KafkaConfiguration) getConfiguration(KafkaConfiguration.class);
        RedisConfiguration redisConfiguration = (RedisConfiguration) ConfigurationHolder.getConfiguration(RedisConfiguration.class);
        ApplicationConfiguration applicationConfiguration = (ApplicationConfiguration) ConfigurationHolder.getConfiguration(ApplicationConfiguration.class);

        String brokerList = configuration.get("kafka.broker.list");
        String redisHost = redisConfiguration.get("redis.host");
        String port = applicationConfiguration.get("cicada.port");

        LOGGER.info("Configuration brokerList=[{}],redisHost=[{}] port=[{}]", brokerList, redisHost, port);
    }

}
