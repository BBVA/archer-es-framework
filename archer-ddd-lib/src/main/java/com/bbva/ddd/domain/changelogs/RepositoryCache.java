package com.bbva.ddd.domain.changelogs;

import com.bbva.dataprocessors.exceptions.StoreNotFoundException;
import com.bbva.dataprocessors.util.ObjectUtils;
import com.bbva.ddd.domain.changelogs.write.ChangelogRecordMetadata;
import com.bbva.ddd.util.StoreUtil;
import org.apache.avro.specific.SpecificRecordBase;

import java.util.HashMap;
import java.util.Map;

public class RepositoryCache<V extends SpecificRecordBase> {
    private final Map<String, Record> records = new HashMap<>();
    private final Map<String, V> currentStates = new HashMap<>();

    @SuppressWarnings("unchecked")
    void updateState(final String key, final V value, final ChangelogRecordMetadata recordMetadata) {
        final Record newRecord = new Record(key, value, recordMetadata);
        final V currentState;

        if (!this.currentStates.containsKey(key) || this.currentStates.get(key) == null || value == null) {
            currentState = value;
        } else {
            currentState = (V) ObjectUtils.merge(currentStates.get(key), value);
        }

        this.currentStates.put(key, currentState);
        records.put(key, newRecord);
    }

    V getCurrentState(final String baseName, final String key) {
        V value;

        if (this.currentStates.containsKey(key)) {
            value = this.currentStates.get(key);
        } else {
            try {
                value = StoreUtil.<String, V>getStore(baseName).findById(key);
            } catch (final StoreNotFoundException e) {
                value = null;
            }
        }
        return value;
    }

    Record getRecord(final String key) {
        return records.get(key);
    }

    public class Record {
        private final String key;
        private final V value;
        private final ChangelogRecordMetadata metadata;

        Record(final String key, final V value, final ChangelogRecordMetadata metadata) {
            this.key = key;
            this.value = value;
            this.metadata = metadata;
        }

        public String key() {
            return key;
        }

        public V value() {
            return value;
        }

        public ChangelogRecordMetadata metadata() {
            return metadata;
        }
    }
}
