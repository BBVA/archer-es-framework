package com.bbva.gateway;

import com.bbva.dataprocessors.DataProcessor;
import com.bbva.ddd.domain.Domain;
import com.bbva.ddd.domain.DomainBuilder;
import com.bbva.ddd.domain.changelogs.exceptions.RepositoryException;
import com.bbva.gateway.config.ConfigBuilder;
import com.bbva.gateway.config.GatewayConfig;
import com.bbva.gateway.consumer.builder.ChangelogKeyBuilder;

import static com.bbva.gateway.constants.Constants.INTERNAL_SUFFIX;
import static com.bbva.gateway.constants.Constants.KEY_SUFFIX;

/**
 * Main class to init gateway with your services
 * For example:
 * <pre>
 * {@code
 *  @Config(file = "example.yml", servicesPackage = "com.example")
 *  class ExampleGateway extends Gateway {
 *      ExampleGateway() {
 *          configure(ExampleGateway.class);
 *          domain = new Domain(new ExampleHandler(servicesPackage, config), config.getAppConfig());
 *          start();
 *          new HelperApplication(config.getAppConfig());
 *      }
 *  }
 * }</pre>
 */
public class Gateway {
    protected static GatewayConfig config;
    protected static Domain domain;

    /**
     * Initialize the domain of the gateway
     *
     * @throws RepositoryException exception
     */
    public void init() throws RepositoryException {
        configure();

        domain = DomainBuilder.create(
                new GatewayHandler(config.getServicesPackage(), config),
                config);
    }

    /**
     * Configure the gateway
     *
     * @param classType class to obtain base package
     */
    protected void configure(final Class classType) {
        config = ConfigBuilder.create(classType);
    }

    /**
     * Configure the gateway
     */
    protected void configure() {
        config = ConfigBuilder.create();
    }

    /**
     * Start domain and processors
     */
    protected void start() {
        DataProcessor.get()
                .add(INTERNAL_SUFFIX + KEY_SUFFIX, new ChangelogKeyBuilder(INTERNAL_SUFFIX + KEY_SUFFIX, INTERNAL_SUFFIX));

        domain.start();
    }
}
