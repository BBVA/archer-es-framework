package com.bbva.ddd.domain.events.read;

import com.bbva.common.config.AppConfig;
import com.bbva.common.consumers.record.CRecord;
import com.bbva.ddd.domain.RunnableConsumer;

import java.util.List;
import java.util.function.Consumer;

/**
 * Specific consumer for event records
 */
public class EventConsumer extends RunnableConsumer<EventHandlerContext> {

    /**
     * Constructor
     *
     * @param id        consumer id
     * @param topics    list of event topics to consume
     * @param callback  callback to manage events produced
     * @param appConfig configuration
     */
    public EventConsumer(final int id, final List<String> topics, final Consumer<EventHandlerContext> callback,
                         final AppConfig appConfig) {
        super(id, topics, callback, appConfig);
    }

    @Override
    public EventHandlerContext context(final CRecord record) {
        final EventHandlerContext eventHandlerContext = new EventHandlerContext(record);
        return eventHandlerContext;
    }

}
