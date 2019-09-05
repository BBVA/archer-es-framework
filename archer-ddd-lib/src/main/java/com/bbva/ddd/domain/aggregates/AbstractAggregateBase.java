package com.bbva.ddd.domain.aggregates;

import com.bbva.common.consumers.CRecord;
import com.bbva.common.producers.ProducerCallback;
import com.bbva.ddd.domain.aggregates.callbacks.ApplyRecordCallback;
import com.bbva.ddd.domain.aggregates.callbacks.DeleteRecordCallback;
import com.bbva.ddd.domain.commands.read.CommandRecord;
import org.apache.avro.specific.SpecificRecordBase;

public abstract class AbstractAggregateBase<K, V extends SpecificRecordBase> implements AggregateBase<K, V> {

    private final V data;
    private final K id;
    private ApplyRecordCallback applyRecordCallback;
    private DeleteRecordCallback<K, V> deleteRecordCallback;

    AbstractAggregateBase(final K id, final V data) {
        this.id = id;
        this.data = data;
    }

    @Override
    public final V getData() {
        return data;
    }

    @Override
    public final K getId() {
        return id;
    }

    @Override
    public void apply(final String method, final V value, final CommandRecord commandRecord,
                      final ProducerCallback callback) {
        applyRecordCallback.apply(method, value, commandRecord, callback);
    }

    public void apply(final String method, final CRecord record, final ProducerCallback callback) {
        deleteRecordCallback.apply(method, (Class<V>) data.getClass(), record, callback);
    }

    @Override
    public final void setApplyRecordCallback(final ApplyRecordCallback apply) {
        applyRecordCallback = apply;
    }

    public final void setDeleteRecordCallback(final DeleteRecordCallback apply) {
        deleteRecordCallback = apply;
    }

    @Override
    public Class<? extends SpecificRecordBase> getValueClass() {
        return data.getClass();
    }

}
