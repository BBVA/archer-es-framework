package com.bbva.ddd.domain.events.write;

import com.bbva.common.producers.PRecordMetadata;
import org.apache.kafka.clients.producer.RecordMetadata;

/**
 * Metadata for events
 */
public class EventRecordMetadata extends PRecordMetadata {

    private final String transactionId;

    /**
     * Constructor
     *
     * @param recordMetadata specific record metadata
     * @param transactionId  event transaction id
     */
    public EventRecordMetadata(final RecordMetadata recordMetadata, final String transactionId) {
        super(recordMetadata);
        this.transactionId = transactionId;
    }

    /**
     * Get the transaction id
     *
     * @return transaction id
     */
    public String transactionId() {
        return transactionId;
    }

}
