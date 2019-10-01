package com.bbva.gateway.config;

import com.bbva.common.config.AppConfiguration;
import com.bbva.common.config.ApplicationConfig;
import com.bbva.gateway.Gateway;
import com.bbva.gateway.config.annotations.Config;
import com.bbva.gateway.config.annotations.ServiceConfig;
import org.reflections.Reflections;
import org.yaml.snakeyaml.Yaml;

import java.util.*;

import static com.bbva.gateway.constants.ConfigConstants.*;

/**
 * Start the general and service config of the gateways
 */
@com.bbva.common.config.Config()
public class Configuration {

    private ApplicationConfig applicationConfig;
    private LinkedHashMap<String, Object> gateway;
    private LinkedHashMap<String, Object> custom;
    private static Configuration instance;

    /**
     * Get a configuration instance
     *
     * @return instance
     */
    public static Configuration get() {
        return instance;
    }

    /**
     * Get the configuration
     *
     * @return configuration
     */
    public ApplicationConfig getApplicationConfig() {
        return applicationConfig;
    }

    /**
     * Set the configuration
     *
     * @param appConfig configuration
     */
    public void setApplicationConfig(final ApplicationConfig appConfig) {
        applicationConfig = appConfig;
    }

    /**
     * Set custom properties
     *
     * @param customConfig custom properties
     */
    public void setCustom(final LinkedHashMap<String, Object> customConfig) {
        custom = customConfig;
    }

    /**
     * Get custom properties
     *
     * @return properties
     */
    public Map<String, Object> getCustom() {
        return custom;
    }

    /**
     * Get gateway config
     *
     * @return properties
     */
    public Map<String, Object> getGateway() {
        return gateway;
    }

    /**
     * Set gateway config
     *
     * @param config properties
     */
    public void setGateway(final LinkedHashMap<String, Object> config) {
        gateway = config;
    }

    /**
     * Initialize gateway with extra config annotation
     *
     * @param extraConfig extra configuration
     * @return configuration instance
     */
    public Configuration init(final Config extraConfig) {

        final Map<String, Object> config = getConfig(extraConfig);
        custom = (LinkedHashMap<String, Object>) config.get(GATEWAY_CUSTOM_PROPERTIES);
        final LinkedHashMap<String, Object> application = (LinkedHashMap<String, Object>) config.get(GATEWAY_APPLICATION_PROPERTIES);
        gateway = (LinkedHashMap<String, Object>) config.get(GATEWAY_GATEWAY_PROPERTIES);

        applicationConfig = new AppConfiguration().init(Configuration.class.getAnnotation(com.bbva.common.config.Config.class));

        addConfigProperties(application, applicationConfig.get(), GATEWAY_APP_PROPERTIES);
        addConfigProperties(application, applicationConfig.consumer().get(), GATEWAY_CONSUMER_PROPERTIES);
        addConfigProperties(application, applicationConfig.producer().get(), GATEWAY_PRODUCER_PROPERTIES);
        addConfigProperties(application, applicationConfig.streams().get(), GATEWAY_STREAM_PROPERTIES);
        addConfigProperties(application, applicationConfig.ksql().get(), GATEWAY_KSQL_PROPERTIES);
        addConfigProperties(application, applicationConfig.dataflow().get(), GATEWAY_DATAFLOW_PROPERTIES);

        instance = this;
        return this;
    }

    private static void addConfigProperties(final LinkedHashMap<String, Object> config, final Properties properties,
                                            final String applicationProperties) {
        if (config.get(applicationProperties) != null) {
            properties.putAll((Map<?, ?>) config.get(GATEWAY_COMMON_PROPERTIES));
            properties.putAll((Map<?, ?>) config.get(applicationProperties));
        }
    }

    private static Map<String, Object> getConfig(final Config extraConfig) {
        final Yaml yaml = new Yaml();
        final AppConfiguration appConfiguration = new AppConfiguration();
        final ClassLoader classLoader = Gateway.class.getClassLoader();
        Map<String, Object> properties = appConfiguration.getConfigFromFile(yaml, classLoader, COMMON_CONFIG);
        if (extraConfig != null) {
            properties = appConfiguration.mergeProperties(properties, appConfiguration.getConfigFromFile(yaml, classLoader, extraConfig.file()));
        }

        properties = appConfiguration.replaceEnvVariables(properties);
        return properties;
    }

    /**
     * Find gateway configuration annotation in all scafolding
     *
     * @return config annotation
     */
    public static Config findConfigAnnotation() {
        final Reflections ref = new Reflections(Gateway.class.getPackage().getName().split("\\.")[0]);
        Config configAnnotation = null;
        for (final Class<?> mainClass : ref.getTypesAnnotatedWith(Config.class)) {
            configAnnotation = mainClass.getAnnotation(Config.class);
        }

        return configAnnotation;
    }

    /**
     * Get all annotated service classes
     *
     * @param servicesPackage main package to find
     * @return list of service classes
     */
    public static List<Class> getServiceClasses(final String servicesPackage) {
        final Reflections ref = new Reflections(!"".equals(servicesPackage) ? servicesPackage : Gateway.class.getPackage().getName().split("\\.")[0]);
        final List<Class> serviceClasses = new ArrayList<>();

        serviceClasses.addAll(ref.getTypesAnnotatedWith(ServiceConfig.class));

        return serviceClasses;
    }

    /**
     * Get specific service configuration
     *
     * @param file file with config
     * @return map of config
     */
    public static LinkedHashMap<String, Object> getServiceConfig(final String file) {
        final Yaml yaml = new Yaml();
        final AppConfiguration appConfiguration = new AppConfiguration();
        final ClassLoader classLoader = Gateway.class.getClassLoader();
        Map<String, Object> properties = appConfiguration.getConfigFromFile(yaml, classLoader, file);

        properties = appConfiguration.replaceEnvVariables(properties);
        return (LinkedHashMap<String, Object>) properties;
    }
}
