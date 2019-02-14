package com.bbva.ddd.domain.events.write;

import com.bbva.common.config.ApplicationConfig;
import com.bbva.common.consumers.CRecord;
import com.bbva.common.producers.CachedProducer;
import com.bbva.common.producers.PRecord;
import com.bbva.common.producers.ProducerCallback;
import com.bbva.common.utils.ByteArrayValue;
import com.bbva.common.utils.RecordHeaders;
import com.bbva.ddd.HelperDomain;
import com.bbva.ddd.domain.events.read.EventRecord;
import com.bbva.ddd.domain.exceptions.ProduceException;
import kst.logging.Logger;
import kst.logging.LoggerFactory;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class Event {

    private static final String TYPE_EVENT_VALUE = "event";

    private static final Logger logger = LoggerFactory.getLogger(Event.class);
    private final CachedProducer producer;
    private final String topic;

    public Event(final String topicBaseName, final ApplicationConfig applicationConfig) {
        this.topic = topicBaseName + ApplicationConfig.EVENTS_RECORD_NAME_SUFFIX;
        producer = new CachedProducer(applicationConfig);
    }

    public <V extends SpecificRecord> EventRecordMetadata send(final String productorName, final V data, final ProducerCallback callback) {
        return generateEvent(null, productorName, data, callback, HelperDomain.get().isReplayMode(), null);
    }

    public <V extends SpecificRecord> EventRecordMetadata send(final String key, final String productorName, final V data,
                                                               final ProducerCallback callback) {
        return generateEvent(key, productorName, data, callback, HelperDomain.get().isReplayMode(), null);
    }

    public <V extends SpecificRecord> EventRecordMetadata send(
            final String productorName, final V data, final ProducerCallback callback, final boolean replay, final String referenceId) {
        return generateEvent(null, productorName, data, callback, replay, referenceId);
    }

    private <V extends SpecificRecord> EventRecordMetadata generateEvent(
            String key, final String productorName, final V record,
            final ProducerCallback callback, final boolean replay, final String referenceId) {
        logger.debug("Generating event by {}", productorName);
        key = (key != null) ? key : UUID.randomUUID().toString();

        final RecordHeaders headers = headers(productorName, replay, referenceId);

        final Future<RecordMetadata> result = producer.add(new PRecord<>(topic, key, record, headers), callback);

        final EventRecordMetadata recordedMessageMetadata;
        try {
            recordedMessageMetadata = new EventRecordMetadata(result.get(), key);
        } catch (final InterruptedException | ExecutionException e) {
            logger.error("Cannot resolve the promise", e);
            throw new ProduceException("Cannot resolve the promise", e);
        }

        logger.info("Event created: {}", key);

        return recordedMessageMetadata;
    }

    private static RecordHeaders headers(final String productorName, final boolean replay, final String referenceId) {

        final RecordHeaders recordHeaders = new RecordHeaders();
        recordHeaders.add(CRecord.TYPE_KEY, new ByteArrayValue(Event.TYPE_EVENT_VALUE));
        recordHeaders.add(EventRecord.PRODUCTOR_NAME_KEY, new ByteArrayValue(productorName));
        recordHeaders.add(CRecord.FLAG_REPLAY_KEY, new ByteArrayValue(replay));
        if (referenceId != null) {
            recordHeaders.add(EventRecord.REFERENCE_ID, new ByteArrayValue(referenceId));
        }

        logger.debug("CRecord getList: {}", recordHeaders.toString());

        return recordHeaders;
    }
}
