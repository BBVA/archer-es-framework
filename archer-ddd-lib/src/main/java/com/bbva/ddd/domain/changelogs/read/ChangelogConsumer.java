package com.bbva.ddd.domain.changelogs.read;

import com.bbva.common.config.AppConfig;
import com.bbva.common.consumers.record.CRecord;
import com.bbva.ddd.domain.RunnableConsumer;

import java.util.List;
import java.util.function.Consumer;

/**
 * Specific consumer for changelog records
 *
 * @param <V> Specific changelog record class
 */
public class ChangelogConsumer extends RunnableConsumer<ChangelogHandlerContext> {

    /**
     * Constructor
     *
     * @param id        id of the consumer
     * @param topics    list of topics to consume
     * @param callback  callback to manage events produced
     * @param appConfig configuration
     */
    public ChangelogConsumer(final int id, final List<String> topics, final Consumer<ChangelogHandlerContext> callback,
                             final AppConfig appConfig) {
        super(id, topics, callback, appConfig);
    }

    @Override
    public ChangelogHandlerContext context(final CRecord record) {
        final ChangelogHandlerContext changelogHandlerContext = new ChangelogHandlerContext(record);
        return changelogHandlerContext;
    }
}
