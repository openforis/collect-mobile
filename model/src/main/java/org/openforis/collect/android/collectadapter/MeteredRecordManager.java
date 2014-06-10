package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.gui.util.meter.Timer;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.RecordPromoteException;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.*;
import org.openforis.collect.persistence.*;
import org.openforis.idm.metamodel.EntityDefinition;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.model.*;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Daniel Wiell
 */
public class MeteredRecordManager extends RecordManager {
    private final RecordManager delegate;

    public MeteredRecordManager(CodeListManager codeListManager, SurveyManager surveyManager) {
        delegate = new RecordManager(false);
        delegate.setCodeListManager(codeListManager);
        delegate.setSurveyManager(surveyManager);
    }

    public void save(final CollectRecord record, final String sessionId) throws RecordPersistenceException {
        time("save", new Callable<Void>() {
            public Void call() throws RecordPersistenceException {
                delegate.save(record, sessionId);
                return null;
            }
        });
    }

    public void delete(final int recordId) throws RecordPersistenceException {
        time("delete", new Callable<Void>() {
            public Void call() throws Exception {
                delegate.delete(recordId);
                return null;
            }
        });
    }

    public void assignOwner(final CollectSurvey survey, final int recordId, final Integer ownerId, final User user, final String sessionId) throws RecordLockedException, MultipleEditException {
        time("assignOwner", new Callable<Void>() {
            public Void call() throws Exception {
                delegate.assignOwner(survey, recordId, ownerId, user, sessionId);
                return null;
            }
        });
    }

    public synchronized CollectRecord checkout(CollectSurvey survey, User user, int recordId, int step, String sessionId, boolean forceUnlock) throws RecordPersistenceException {
        throw new UnsupportedOperationException("Deprecated");
    }

    public synchronized CollectRecord checkout(final CollectSurvey survey, final User user, final int recordId, final CollectRecord.Step step, final String sessionId, final boolean forceUnlock) throws RecordPersistenceException {
        return time("checkout", new Callable<CollectRecord>() {
            public CollectRecord call() throws Exception {
                return delegate.checkout(survey, user, recordId, step, sessionId, forceUnlock);
            }
        });
    }

    public CollectRecord load(CollectSurvey survey, int recordId, int step) {
        throw new UnsupportedOperationException("Deprecated");
    }

    public CollectRecord load(final CollectSurvey survey, final int recordId, final CollectRecord.Step step) {
        return time("load", new Callable<CollectRecord>() {
            public CollectRecord call() throws Exception {
                return delegate.load(survey, recordId, step);
            }
        });
    }

    public List<CollectRecord> loadSummaries(final CollectSurvey survey, final String rootEntity) {
        return time("loadSummaries(survey, rootEntity)", new Callable<List<CollectRecord>>() {
            public List<CollectRecord> call() throws Exception {
                return delegate.loadSummaries(survey, rootEntity);
            }
        });
    }

    public List<CollectRecord> loadSummaries(final CollectSurvey survey, final String rootEntity, final String... keys) {
        return time("loadSummaries(survey, rootEntity, keys)", new Callable<List<CollectRecord>>() {
            public List<CollectRecord> call() throws Exception {
                return delegate.loadSummaries(survey, rootEntity, keys);
            }
        });
    }

    public List<CollectRecord> loadSummaries(final CollectSurvey survey, final String rootEntity, final int offset, final int maxNumberOfRecords, final List<RecordSummarySortField> sortFields, final String... keyValues) {
        return time("loadSummaries(survey, rootEntity, offset, maxNumberOfRecords, sortFields, keyValues)", new Callable<List<CollectRecord>>() {
            public List<CollectRecord> call() throws Exception {
                return delegate.loadSummaries(survey, rootEntity, offset, maxNumberOfRecords, sortFields, keyValues);
            }
        });
    }

    public int countRecords(final CollectSurvey survey) {
        return time("countRecords(survey)", new Callable<Integer>() {
            public Integer call() throws Exception {
                return delegate.countRecords(survey);
            }
        });
    }

    public int countRecords(final CollectSurvey survey, final int rootEntityDefinitionId) {
        return time("countRecords(survey, rootEntityDefinitionId)", new Callable<Integer>() {
            public Integer call() throws Exception {
                return delegate.countRecords(survey, rootEntityDefinitionId);
            }
        });
    }

    public CollectRecord create(final CollectSurvey survey, final String rootEntityName, final User user, final String modelVersionName) throws RecordPersistenceException {
        return time("create(survey, rootEntityName, user, modelVersionName)", new Callable<CollectRecord>() {
            public CollectRecord call() throws Exception {
                return delegate.create(survey, rootEntityName, user, modelVersionName);
            }
        });
    }

    public CollectRecord create(final CollectSurvey survey, final EntityDefinition rootEntityDefinition, final User user, final String modelVersionName, final String sessionId) throws RecordPersistenceException {
        return time("create(survey, rootEntityDefinition, user, modelVersionName, sessionId)", new Callable<CollectRecord>() {
            public CollectRecord call() throws Exception {
                return delegate.create(survey, rootEntityDefinition, user, modelVersionName, sessionId);
            }
        });
    }

    public CollectRecord create(final CollectSurvey survey, final String rootEntityName, final User user, final String modelVersionName, final String sessionId) throws RecordPersistenceException {
        return time("create(survey, rootEntityName, user, modelVersionName, sessionId)", new Callable<CollectRecord>() {
            public CollectRecord call() throws Exception {
                return delegate.create(survey, rootEntityName, user, modelVersionName, sessionId);
            }
        });
    }

    public void promote(final CollectRecord record, final User user) throws RecordPromoteException, MissingRecordKeyException {
        time("promote(record, user)", new Callable<Void>() {
            public Void call() throws Exception {
                delegate.promote(record, user);
                return null;
            }
        });
    }

    public void demote(final CollectSurvey survey, final int recordId, final CollectRecord.Step currentStep, final User user) throws RecordPersistenceException {
        time("demote(survey, recordId, currentStep, user)", new Callable<Void>() {
            public Void call() throws Exception {
                delegate.demote(survey, recordId, currentStep, user);
                return null;
            }
        });
    }

    public void validateAndSave(final CollectSurvey survey, final User user, final String sessionId, final int recordId, final CollectRecord.Step step) throws RecordLockedException, MultipleEditException {
        time("validateAndSave(survey, user, sessionId, recordId, step)", new Callable<Void>() {
            public Void call() throws RecordLockedException, MultipleEditException {
                delegate.validateAndSave(survey, user, sessionId, recordId, step);
                return null;
            }
        });
    }

    public <V extends Value> NodeChangeSet updateAttribute(final Attribute<? extends NodeDefinition, V> attribute, final V value) {
        return time("updateAttribute(attribute, value)", new Callable<NodeChangeSet>() {
            public NodeChangeSet call() throws Exception {
                return delegate.updateAttribute(attribute, value);
            }
        });
    }

    public NodeChangeSet updateAttribute(final Attribute<?, ?> attribute, final FieldSymbol symbol) {
        return time("updateAttribute(attribute, symbol)", new Callable<NodeChangeSet>() {
            public NodeChangeSet call() throws Exception {
                return delegate.updateAttribute(attribute, symbol);
            }
        });
    }

    public <V> NodeChangeSet updateField(final Field<V> field, final V value) {
        return time("updateField(field, value)", new Callable<NodeChangeSet>() {
            public NodeChangeSet call() throws Exception {
                return delegate.updateField(field, value);
            }
        });
    }

    public <V> NodeChangeSet updateField(final Field<V> field, final FieldSymbol symbol) {
        return time("updateField(field, symbol)", new Callable<NodeChangeSet>() {
            public NodeChangeSet call() throws Exception {
                return delegate.updateField(field, symbol);
            }
        });
    }

    public NodeChangeSet addEntity(final Entity parentEntity, final String nodeName) {
        return time("addEntity(parentEntity, nodeName)", new Callable<NodeChangeSet>() {
            public NodeChangeSet call() throws Exception {
                return delegate.addEntity(parentEntity, nodeName);
            }
        });
    }

    public NodeChangeSet addAttribute(final Entity parentEntity, final String attributeName, final Value value, final FieldSymbol symbol, final String remarks) {
        return time("addAttribute(parentEntity, attributeName, value, symbol, remarks)", new Callable<NodeChangeSet>() {
            public NodeChangeSet call() throws Exception {
                return delegate.addAttribute(parentEntity, attributeName, value, symbol, remarks);
            }
        });
    }

    public NodeChangeSet updateRemarks(final Field<?> field, final String remarks) {
        return time("updateRemarks(field, remarks)", new Callable<NodeChangeSet>() {
            public NodeChangeSet call() throws Exception {
                return delegate.updateRemarks(field, remarks);
            }
        });
    }

    public NodeChangeSet approveMissingValue(final Entity parentEntity, final String nodeName) {
        return time("approveMissingValue", new Callable<NodeChangeSet>() {
            public NodeChangeSet call() throws Exception {
                return delegate.approveMissingValue(parentEntity, nodeName);
            }
        });
    }

    public NodeChangeSet confirmError(final Attribute<?, ?> attribute) {
        return time("confirmError", new Callable<NodeChangeSet>() {
            public NodeChangeSet call() throws Exception {
                return delegate.confirmError(attribute);
            }
        });
    }

    public void validate(final CollectRecord record) {
        time("validate", new Runnable() {
            public void run() {
                delegate.validate(record);
            }
        });
    }

    public NodeChangeSet deleteNode(final Node<?> node) {
        return time("deleteNode", new Callable<NodeChangeSet>() {
            public NodeChangeSet call() throws Exception {
                return delegate.deleteNode(node);
            }
        });
    }

    public NodeChangeSet applyDefaultValue(final Attribute<?, ?> attribute) {
        return time("applyDefaultValue", new Callable<NodeChangeSet>() {
            public NodeChangeSet call() throws Exception {
                return delegate.applyDefaultValue(attribute);
            }
        });
    }

    public void moveNode(final CollectRecord record, final int nodeId, final int index) {
        time("moveNode", new Runnable() {
            public void run() {
                delegate.moveNode(record, nodeId, index);
            }
        });
    }

    public void checkIsLocked(final int recordId, final User user, final String lockId) throws RecordUnlockedException {
        time("checkIsLocked", new Callable<Void>() {
            public Void call() throws RecordUnlockedException {
                delegate.checkIsLocked(recordId, user, lockId);
                return null;
            }
        });
    }

    public void releaseLock(final Integer recordId) {
        time("releaseLock", new Runnable() {
            public void run() {
                delegate.releaseLock(recordId);
            }
        });
    }

    public long getLockTimeoutMillis() {
        return delegate.getLockTimeoutMillis();
    }

    public void setLockTimeoutMillis(long timeoutMillis) {
        delegate.setLockTimeoutMillis(timeoutMillis);
    }

    public boolean isLockingEnabled() {
        return delegate.isLockingEnabled();
    }

    public void setLockingEnabled(boolean lockingEnabled) {
        delegate.setLockingEnabled(lockingEnabled);
    }

    public RecordDao getRecordDao() {
        return delegate.getRecordDao();
    }

    public void setRecordDao(RecordDao recordDao) {
        delegate.setRecordDao(recordDao);
    }

    public void setCodeListManager(CodeListManager codeListManager) {
        delegate.setCodeListManager(codeListManager);
    }

    public void setSurveyManager(SurveyManager surveyManager) {
        delegate.setSurveyManager(surveyManager);
    }

    private <T> T time(String methodName, Callable<T> action) {
        return Timer.time(RecordManager.class, methodName, action);
    }

    private void time(String methodName, Runnable action) {
        Timer.time(RecordManager.class, methodName, action);
    }
}
