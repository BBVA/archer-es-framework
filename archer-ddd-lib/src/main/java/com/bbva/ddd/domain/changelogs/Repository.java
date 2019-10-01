package com.bbva.ddd.domain.changelogs;

import com.bbva.common.config.ApplicationConfig;
import com.bbva.common.consumers.CRecord;
import com.bbva.common.exceptions.ApplicationException;
import com.bbva.common.producers.CachedProducer;
import com.bbva.common.producers.PRecord;
import com.bbva.common.producers.ProducerCallback;
import com.bbva.common.utils.headers.RecordHeaders;
import com.bbva.common.utils.headers.types.ChangelogHeaderType;
import com.bbva.common.utils.headers.types.CommandHeaderType;
import com.bbva.common.utils.headers.types.CommonHeaderType;
import com.bbva.ddd.domain.HelperDomain;
import com.bbva.ddd.domain.aggregates.AbstractAggregateBase;
import com.bbva.ddd.domain.aggregates.AggregateBase;
import com.bbva.ddd.domain.aggregates.annotations.Aggregate;
import com.bbva.ddd.domain.aggregates.annotations.AggregateParent;
import com.bbva.ddd.domain.aggregates.exceptions.AggregateDependenciesException;
import com.bbva.ddd.domain.changelogs.write.ChangelogRecordMetadata;
import com.bbva.ddd.domain.commands.read.CommandRecord;
import com.bbva.logging.Logger;
import com.bbva.logging.LoggerFactory;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.RecordMetadata;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class Repository<K, V extends SpecificRecordBase> {

    private static final Logger logger = LoggerFactory.getLogger(Repository.class);

    private final Class<? extends AggregateBase> aggregateClass;
    private final CachedProducer producer;
    private final String changelogName;
    private final String baseName;
    private final String aggregateUUID;
    private final RepositoryCache<V> repositoryCache;

    private String parentChangelogName;
    private Field expectedParentField;
    private Class<? extends SpecificRecordBase> parentValueClass;

    public Repository(final String baseName, final Class<? extends AggregateBase> aggregateClass,
                      final ApplicationConfig applicationConfig) throws AggregateDependenciesException {
        aggregateUUID = UUID.randomUUID().toString();
        this.aggregateClass = aggregateClass;
        producer = new CachedProducer(applicationConfig);
        this.baseName = baseName;
        changelogName = baseName + ApplicationConfig.CHANGELOG_RECORD_NAME_SUFFIX;
        repositoryCache = new RepositoryCache<>();

        setDependencies();

        logger.info("Repository for {} initialized", aggregateClass.getName());
    }

    public String getBaseName() {
        return baseName;
    }

    public AggregateBase create(final String aggregateKey, final V value, final CommandRecord referenceRecord,
                                final ProducerCallback callback) {
        final String key = aggregateKey == null ? referenceRecord.entityUuid() : aggregateKey;

        try {
            value.put("uuid", key);
        } catch (final NullPointerException e) {
            logger.warn("Schema " + value.getSchema().getName() + " has not field uuid");
        }

        final AggregateBase aggregateBaseInstance = getAggregateIntance(key, value);

        logger.debug("Creating PRecord of type {}", value.getClass().getName());

        save((String) aggregateBaseInstance.getId(), (V) aggregateBaseInstance.getData(), referenceRecord, "constructor",
                callback);

        return aggregateBaseInstance;
    }

    public AggregateBase loadFromStore(final String key) {
        logger.debug("Loading from store {}", baseName);

        final V value = repositoryCache.getCurrentState(baseName, key);

        if (value != null) {
            logger.debug("Value found for id {}", key);
            return getAggregateIntance(key, value);
        }
        return null;
    }

    private void setDependencies() throws AggregateDependenciesException {

        if (aggregateClass.isAnnotationPresent(AggregateParent.class)) {

            final Class<? extends SpecificRecordBase> childValueClass = getValueClass(aggregateClass);

            final Class<? extends AbstractAggregateBase> parentClass = aggregateClass
                    .getAnnotation(AggregateParent.class).value();
            parentValueClass = getValueClass(parentClass);

            final Field[] parentFields = parentValueClass.getDeclaredFields();
            for (final Field parentField : parentFields) {
                if (Modifier.isPublic(parentField.getModifiers())
                        && childValueClass.isAssignableFrom(parentField.getType())) {
                    parentChangelogName = parentClass.getAnnotation(Aggregate.class).baseName()
                            + ApplicationConfig.CHANGELOG_RECORD_NAME_SUFFIX;
                    expectedParentField = parentField;
                    break;
                }
            }
            if (expectedParentField == null) {
                throw new AggregateDependenciesException("Something rare happened: Parent has not field for child");
            }
        }
    }

    private static Class getValueClass(final Class anotatedClass) {
        return (Class) ((ParameterizedType) anotatedClass.getGenericSuperclass()).getActualTypeArguments()[1];
    }

    private ChangelogRecordMetadata save(final String key, final V value, final CRecord referenceRecord, final String method,
                                         final ProducerCallback callback) {
        ChangelogRecordMetadata changelogRecordMetadata = null;

        if (parentValueClass != null && expectedParentField != null) {
            try {
                final SpecificRecordBase parentValue = parentValueClass.getConstructor().newInstance();
                expectedParentField.set(parentValue, value);
                changelogRecordMetadata = propagate(parentChangelogName, key, parentValue, headers(method, referenceRecord),
                        (id, e) -> {
                            if (e != null) {
                                logger.error("Error saving the object", e);
                            } else {
                                logger.info("Parent updated");
                                propagate(changelogName, key, value, headers(method, referenceRecord), callback);
                            }
                        });
            } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                logger.error("Problems saving the object", e);
            }
        } else {
            changelogRecordMetadata = propagate(changelogName, key, value, headers(method, referenceRecord), callback);
        }

        repositoryCache.updateState(key, value, changelogRecordMetadata);

        return changelogRecordMetadata;
    }

    private ChangelogRecordMetadata propagate(final String changelogName, final String key,
                                              final SpecificRecordBase value, final RecordHeaders headers, final ProducerCallback callback) {
        logger.info("Propagate PRecord for key: {}", key);
        ChangelogRecordMetadata changelogMessageMetadata = null;
        final PRecord<String, SpecificRecordBase> record = new PRecord<>(changelogName, key, value, headers);

        try {
            final Future<RecordMetadata> result = producer.add(record, callback);
            changelogMessageMetadata = new ChangelogRecordMetadata(result.get());

        } catch (final InterruptedException | ExecutionException e) {
            logger.error("Problems propagating the object", e);
        }

        return changelogMessageMetadata;
    }

    private ChangelogRecordMetadata delete(final String key, final Class<V> valueClass, final RecordHeaders headers,
                                           final ProducerCallback callback) {

        logger.info("Delete PRecord for key: {}", key);

        ChangelogRecordMetadata changelogRecordMetadata = null;
        final PRecord<String, V> record = new PRecord<>(changelogName, key, null, headers);

        try {
            final Future<RecordMetadata> result = producer.remove(record, valueClass, callback);
            changelogRecordMetadata = new ChangelogRecordMetadata(result.get());

        } catch (final InterruptedException | ExecutionException e) {
            logger.error("Problems deleting the object", e);
        }

        repositoryCache.updateState(key, null, changelogRecordMetadata);

        return changelogRecordMetadata;
    }

    private AggregateBase getAggregateIntance(final String key, final V value) {
        final AggregateBase aggregateBaseInstance;
        try {
            aggregateBaseInstance = aggregateClass.getConstructor(key.getClass(), value.getClass())
                    .newInstance(key, value);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("Not constructor found for aggregate class", e);
            throw new ApplicationException("Not constructor found for aggregate class", e);
        }

        logger.info("Aggregate loaded");

        aggregateBaseInstance.setApplyRecordCallback(
                (method, newValue, referenceRecord, callback) ->
                        save((String) aggregateBaseInstance.getId(), (V) newValue, referenceRecord, method, callback));

        if (aggregateBaseInstance instanceof AbstractAggregateBase) {
            ((AbstractAggregateBase) aggregateBaseInstance).setDeleteRecordCallback(
                    (method, valueClass, referenceRecord, callback) ->
                            delete((String) aggregateBaseInstance.getId(), valueClass, headers(method, referenceRecord), callback));
        }

        logger.debug("Returning Aggregate instance");
        return aggregateBaseInstance;
    }

    private RecordHeaders headers(final String aggregateMethod, final CRecord referenceRecord) {

        final RecordHeaders recordHeaders = new RecordHeaders();
        recordHeaders.add(CommonHeaderType.TYPE_KEY, ChangelogHeaderType.TYPE_VALUE);
        recordHeaders.add(ChangelogHeaderType.UUID_KEY, referenceRecord.recordHeaders().find(CommandHeaderType.ENTITY_UUID_KEY));

        if (referenceRecord != null) {
            recordHeaders.add(CommonHeaderType.REFERENCE_RECORD_KEY_KEY, referenceRecord.key());
            recordHeaders.add(CommonHeaderType.REFERENCE_RECORD_TYPE_KEY,
                    referenceRecord.recordHeaders().find(CommonHeaderType.TYPE_KEY).asString());
            recordHeaders.add(CommonHeaderType.REFERENCE_RECORD_POSITION_KEY,
                    referenceRecord.topic() + "-" + referenceRecord.partition() + "-" + referenceRecord.offset());
        }

        recordHeaders.add(CommonHeaderType.FLAG_REPLAY_KEY,
                (referenceRecord != null && referenceRecord.isReplayMode()) || HelperDomain.get().isReplayMode());
        recordHeaders.add(ChangelogHeaderType.AGGREGATE_UUID_KEY, aggregateUUID);
        recordHeaders.add(ChangelogHeaderType.AGGREGATE_NAME_KEY, aggregateClass.getName());
        recordHeaders.add(ChangelogHeaderType.AGGREGATE_METHOD_KEY, aggregateMethod);

        logger.debug("CRecord getList: {}", recordHeaders.toString());

        return recordHeaders;
    }
}
