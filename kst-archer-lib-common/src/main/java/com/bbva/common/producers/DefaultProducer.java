package com.bbva.common.producers;

import com.bbva.common.config.ApplicationConfig;
import kst.logging.Logger;
import kst.logging.LoggerFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.AuthorizationException;
import org.apache.kafka.common.errors.OutOfOrderSequenceException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.concurrent.Future;

public class DefaultProducer<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProducer.class);
    private final Producer<K, V> producer;

    public DefaultProducer(final String producerName, final ApplicationConfig applicationConfig, final Serializer<K> serializedKey,
                           final Serializer<V> serializedValue) {

        final String transactionalIdPrefix = applicationConfig.producer().get(ApplicationConfig.ProducerProperties.TRANSACTIONAL_ID_PREFIX).toString();
        applicationConfig.producer().put(ApplicationConfig.ProducerProperties.TRANSACTIONAL_ID, transactionalIdPrefix + producerName);
        producer = new KafkaProducer<>(applicationConfig.producer().get(), serializedKey, serializedValue);
        producer.initTransactions();
    }

    public Future<RecordMetadata> save(final PRecord<K, V> record, final ProducerCallback callback) {
        logger.debug("Produce generic PRecord with key {}", record.key());

        Future<RecordMetadata> result = null;
        try {
            producer.beginTransaction();
            result = producer.send(record, (metadata, e) -> {
                if (e != null) {
                    logger.error("Error producing key " + record.key(), e);
                    producer.abortTransaction();
                } else {
                    logger.info("PRecord Produced. key {}", record.key());
                }
                callback.onCompletion(record.key(), e);
            });

            producer.commitTransaction();
            producer.flush();

            logger.debug("End of production");

        } catch (final ProducerFencedException | OutOfOrderSequenceException | AuthorizationException e) {
            // We can't recover from these exceptions, so our only option is to close the producer and exit.
            producer.close();
            throw e;
        } catch (final KafkaException e) {
            producer.abortTransaction();
        }
        return result;
    }

}
