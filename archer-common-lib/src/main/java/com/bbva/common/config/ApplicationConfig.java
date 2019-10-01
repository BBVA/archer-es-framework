package com.bbva.common.config;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

/**
 * Class that s torage all application config
 */
public class ApplicationConfig implements Cloneable {

    public static final String SCHEMA_REGISTRY_URL = "schema.registry.url";
    public static final String REPLAY_TOPICS = "replay.topics";
    public static final String REPLICATION_FACTOR = "replication.factor";
    public static final String PARTITIONS = "partitions";

    public static final String CHANGELOG_RECORD_NAME_SUFFIX = "_data_changelog";
    public static final String COMMANDS_RECORD_NAME_SUFFIX = "_commands";
    public static final String EVENTS_RECORD_NAME_SUFFIX = "_events";
    public static final String COMMON_RECORD_TYPE = "common";
    public static final String CHANGELOG_RECORD_TYPE = "data_changelog";
    public static final String COMMANDS_RECORD_TYPE = "commands";
    public static final String EVENTS_RECORD_TYPE = "events";
    public static final String SNAPSHOT_RECORD_TYPE = "snapshot";
    public static final String STORE_NAME_SUFFIX = "_store";
    public static final String INTERNAL_NAME_PREFIX = "internal_";

    private Properties applicationProperties = new Properties();
    private final ProducerProperties producerProperties = new ProducerProperties();
    private final ConsumerProperties consumerProperties = new ConsumerProperties();
    private final StreamsProperties streamsProperties = new StreamsProperties();
    private final KsqlProperties ksqlProperties = new KsqlProperties();
    private final DataflowProperties dataflowProperties = new DataflowProperties();

    /**
     * Clone the application properties un in memory map
     *
     * @param applicationProperties application properties
     */
    public void put(final Properties applicationProperties) {
        this.applicationProperties = (Properties) applicationProperties.clone();
    }

    /**
     * Put a new key/value in the properties map
     *
     * @param key   key
     * @param value value
     * @param <V>   Type of the value
     */
    public <V> void put(final String key, final V value) {
        applicationProperties.put(key, value);
    }

    /**
     * Get the value of the key
     *
     * @param key specific key
     * @return the value
     */
    public Object get(final String key) {
        return applicationProperties.getProperty(key);
    }

    /**
     * Check if the properties contains the key
     *
     * @param key key to find
     * @return true/false
     */
    public boolean contains(final String key) {
        return applicationProperties.containsKey(key);
    }

    /**
     * get intetger valuie of the key
     *
     * @param key key
     * @return value
     */
    public Integer getInteger(final String key) {
        return Integer.valueOf(applicationProperties.getProperty(key));
    }

    /**
     * Get application properties
     *
     * @return properties
     */
    public Properties get() {
        return applicationProperties;
    }

    /**
     * Return producer properties
     *
     * @return properties
     */
    public ProducerProperties producer() {
        return producerProperties;
    }

    /**
     * Return consumer properties
     *
     * @return properties
     */
    public ConsumerProperties consumer() {
        return consumerProperties;
    }

    /**
     * Return stream properties
     *
     * @return properties
     */
    public StreamsProperties streams() {
        return streamsProperties;
    }

    /**
     * Return ksql properties
     *
     * @return properties
     */
    public KsqlProperties ksql() {
        return ksqlProperties;
    }

    /**
     * Return dataflow properties
     *
     * @return properties
     */
    public DataflowProperties dataflow() {
        return dataflowProperties;
    }

    /**
     * Producer properties
     */
    final public class ProducerProperties extends PropertiesClass {

        public static final String BOOTSTRAP_SERVERS = CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
        public static final String PRODUCER_ACKS = ProducerConfig.ACKS_CONFIG;
        public static final String PRODUCER_RETRIES = ProducerConfig.RETRIES_CONFIG;
        public static final String INTERCEPTOR_CLASSES = ProducerConfig.INTERCEPTOR_CLASSES_CONFIG;
        public static final String ENABLE_IDEMPOTENCE = ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG;
        public static final String TRANSACTIONAL_ID_PREFIX = "transactional.id.prefix";
        public static final String TRANSACTIONAL_ID = ProducerConfig.TRANSACTIONAL_ID_CONFIG;

    }

    /**
     * Consumer properties
     */
    final public class ConsumerProperties extends PropertiesClass {

        public static final String BOOTSTRAP_SERVERS = CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
        public static final String CONSUMER_GROUP_ID = ConsumerConfig.GROUP_ID_CONFIG;
        public static final String CLIENT_ID = ConsumerConfig.CLIENT_ID_CONFIG;
        public static final String AUTO_OFFSET_RESET = ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
        public static final String ENABLE_AUTO_COMMIT = ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG;
        public static final String AUTO_COMMIT_INTERVAL_MS = ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG;
        public static final String SESSION_TIMEOUT_MS = ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG;
        public static final String INTERCEPTOR_CLASSES = ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG;
        public static final String ISOLATION_LEVEL = ConsumerConfig.ISOLATION_LEVEL_CONFIG;

    }

    /**
     * Stream properties
     */
    final public class StreamsProperties extends PropertiesClass {

        public static final String BOOTSTRAP_SERVERS = CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG;
        public static final String GROUP_ID = ConsumerConfig.GROUP_ID_CONFIG;
        public static final String APPLICATION_ID = StreamsConfig.APPLICATION_ID_CONFIG;
        public static final String APPLICATION_NAME = "application.name";
        public static final String APPLICATION_SERVER = StreamsConfig.APPLICATION_SERVER_CONFIG;
        public static final String CLIENT_ID = ConsumerConfig.CLIENT_ID_CONFIG;
        public static final String STREAMS_AUTO_OFFSET_RESET = ConsumerConfig.AUTO_OFFSET_RESET_CONFIG;
        public static final String COMMIT_INTERVAL_MS = StreamsConfig.COMMIT_INTERVAL_MS_CONFIG;
        public static final String INTERNAL_REPLICATION_FACTOR = StreamsConfig.REPLICATION_FACTOR_CONFIG;
        public static final String MAX_POLL_RECORDS = ConsumerConfig.MAX_POLL_RECORDS_CONFIG;
        public static final String MAX_POLL_INTERVAL_MS = ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG;
        public static final String SESSION_TIMEOUT_MS = ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG;
        public static final String STATE_DIR = StreamsConfig.STATE_DIR_CONFIG;
        public static final String PROCESSING_GUARANTEE = StreamsConfig.PROCESSING_GUARANTEE_CONFIG;
        public static final String PRODUCER_INTERCEPTOR_CLASSES = StreamsConfig.PRODUCER_PREFIX
                + ProducerConfig.INTERCEPTOR_CLASSES_CONFIG;
        public static final String CONSUMER_INTERCEPTOR_CLASSES = StreamsConfig.CONSUMER_PREFIX
                + ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG;

    }

    /**
     * Ksql properties
     */
    final public class KsqlProperties extends PropertiesClass {

        public static final String KSQL_PREFIX = "ksql_";
        public static final String KSQL_SUFFIX = "_ksql";
        public static final String KSQL_TABLE_SUFFIX = KSQL_SUFFIX + "_table";
        public static final String KSQL_STREAM_SUFFIX = KSQL_SUFFIX + "_stream";

    }

    /**
     * Dataflow properties
     */
    final public class DataflowProperties extends PropertiesClass {

    }

    /**
     * General class for properties
     */
    public class PropertiesClass {

        private Properties properties = new Properties();

        /**
         * Put custom properties in memory
         *
         * @param properties new properties
         */
        public void put(final Properties properties) {
            this.properties = (Properties) properties.clone();
        }

        /**
         * Add/Replace specific property
         *
         * @param key   key
         * @param value value
         * @param <V>   class type of value
         */
        public <V> void put(final String key, final V value) {
            properties.put(key, value);
        }

        /**
         * Get properties
         *
         * @return properties
         */
        public Properties get() {
            return properties;
        }

        /**
         * Get value of property
         *
         * @param property key
         * @return value
         */
        public Object get(final String property) {
            return properties.get(property);
        }

    }
}
