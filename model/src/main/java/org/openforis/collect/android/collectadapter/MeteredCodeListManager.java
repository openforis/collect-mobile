package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.gui.util.meter.Timer;
import org.openforis.collect.io.exception.CodeListImportException;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.CodeListItemDao;
import org.openforis.collect.persistence.DatabaseExternalCodeListProvider;
import org.openforis.idm.metamodel.*;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Daniel Wiell
 */
public class MeteredCodeListManager extends CodeListManager {
    private final CodeListManager delegate;

    public MeteredCodeListManager(CodeListItemDao codeListItemDao, DatabaseExternalCodeListProvider externalCodeListProvider) {
        delegate = new CodeListManager();
        delegate.setCodeListItemDao(codeListItemDao);
        delegate.setExternalCodeListProvider(externalCodeListProvider);
    }

    public void importCodeLists(final CollectSurvey survey, final InputStream is) {
        time("importCodeLists(survey, inputStream)", new Callable<Void>() {
            public Void call() throws CodeListImportException {
                delegate.importCodeLists(survey, is);
                return null;
            }
        });
    }

    public void importCodeLists(final CollectSurvey survey, final File file) throws CodeListImportException {
        time("importCodeLists(survey, file)", new Callable<Void>() {
            public Void call() throws CodeListImportException {
                delegate.importCodeLists(survey, file);
                return null;
            }
        });
    }

    public <T extends CodeListItem> T loadItemByAttribute(final CodeAttribute attribute) {
        return time("loadItemByAttribute", new Callable<T>() {
            public T call() throws Exception {
                return delegate.loadItemByAttribute(attribute);
            }
        });
    }

    public <T extends CodeListItem> List<T> loadItems(final CodeList list, final int level) {
        return time("loadItems", new Callable<List<T>>() {
            public List<T> call() throws Exception {
                return delegate.loadItems(list, level);
            }
        });
    }

    public <T extends CodeListItem> List<T> loadRootItems(final CodeList list) {
        return time("loadRootItems", new Callable<List<T>>() {
            public List<T> call() throws Exception {
                return delegate.loadRootItems(list);
            }
        });
    }

    public <T extends CodeListItem> T loadRootItem(final CodeList list, final String code, final ModelVersion version) {
        return time("loadRootItem", new Callable<T>() {
            public T call() throws Exception {
                return delegate.loadRootItem(list, code, version);
            }
        });
    }

    public CodeListItem findValidItem(final Entity parent, final CodeAttributeDefinition defn, final String code) {
        return time("findValidItem", new Callable<CodeListItem>() {
            public CodeListItem call() throws Exception {
                return delegate.findValidItem(parent, defn, code);
            }
        });
    }

    public List<CodeListItem> findValidItems(final Entity parent, final CodeAttributeDefinition defn, final String... codes) {
        return time("findValidItems", new Callable<List<CodeListItem>>() {
            public List<CodeListItem> call() throws Exception {
                return delegate.findValidItems(parent, defn, codes);
            }
        });
    }

    public List<CodeListItem> loadValidItems(final Entity parent, final CodeAttributeDefinition def) {
        return Timer.time(MeteredCodeListManager.class, "loadValidItems", new Callable<List<CodeListItem>>() {
            public List<CodeListItem> call() {
                return delegate.loadValidItems(parent, def);
            }
        });
    }

    public <T extends CodeListItem> List<T> loadChildItems(final CodeListItem parent) {
        return time("loadChildItems", new Callable<List<T>>() {
            public List<T> call() throws Exception {
                return delegate.loadChildItems(parent);
            }
        });
    }

    public <T extends CodeListItem> T loadChildItem(final CodeList list, final String code, final ModelVersion version) {
        return time("loadChildItem(list, code, version)", new Callable<T>() {
            public T call() throws Exception {
                return delegate.loadChildItem(list, code, version);
            }
        });
    }

    public boolean hasChildItems(final CodeListItem parent) {
        return time("hasChildItems", new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return delegate.hasChildItems(parent);
            }
        });
    }

    public boolean hasQualifiableItems(final CodeList list) {
        return time("hasQualifiableItems", new Callable<Boolean>() {
            public Boolean call() throws Exception {
                return delegate.hasQualifiableItems(list);
            }
        });
    }

    public <T extends CodeListItem> T loadChildItem(final T parent, final String code, final ModelVersion version) {
        return time("loadChildItem(parent, code, version)", new Callable<T>() {
            public T call() throws Exception {
                return delegate.loadChildItem(parent, code, version);
            }
        });
    }

    public <T extends CodeListItem> T loadParentItem(final T item) {
        return time("loadParentItem", new Callable<T>() {
            public T call() throws Exception {
                return delegate.loadParentItem(item);
            }
        });
    }

    public void publishCodeLists(final int surveyWorkId, final int publishedSurveyId) {
        time("publishCodeLists", new Runnable() {
            public void run() {
                delegate.publishCodeLists(surveyWorkId, publishedSurveyId);
            }
        });
    }

    public void cloneCodeLists(final CollectSurvey fromSurvey, final CollectSurvey toSurvey) {
        time("cloneCodeLists", new Runnable() {
            public void run() {
                delegate.cloneCodeLists(fromSurvey, toSurvey);
            }
        });
    }

    public void save(final PersistedCodeListItem item) {
        time("save(item)", new Runnable() {
            public void run() {
                delegate.save(item);
            }
        });
    }

    public void save(final List<PersistedCodeListItem> items) {
        time("save(items)", new Runnable() {
            public void run() {
                delegate.save(items);
            }
        });
    }

    public void saveItemsAndDescendants(final List<CodeListItem> items) {
        time("saveItemsAndDescendants", new Runnable() {
            public void run() {
                delegate.saveItemsAndDescendants(items);
            }
        });
    }

    public void delete(final CodeList codeList) {
        time("delete(codeList)", new Runnable() {
            public void run() {
                delegate.delete(codeList);
            }
        });
    }

    public void delete(final CodeListItem item) {
        time("delete(item)", new Runnable() {
            public void run() {
                delegate.delete(item);
            }
        });
    }

    public void deleteAllItems(final CodeList list) {
        time("deleteAllItems", new Runnable() {
            public void run() {
                delegate.deleteAllItems(list);
            }
        });
    }

    public void deleteAllItemsBySurvey(final int surveyId, final boolean work) {
        time("deleteAllItemsBySurvey", new Runnable() {
            public void run() {
                delegate.deleteAllItemsBySurvey(surveyId, work);
            }
        });
    }

    public void deleteInvalidCodeListReferenceItems(final CollectSurvey survey) {
        time("deleteInvalidCodeListReferenceItems", new Runnable() {
            public void run() {
                delegate.deleteInvalidCodeListReferenceItems(survey);
            }
        });
    }

    public void shiftItem(final CodeListItem item, final int indexTo) {
        time("shiftItem", new Runnable() {
            public void run() {
                delegate.shiftItem(item, indexTo);
            }
        });
    }

    public int nextSystemId() {
        return time("nextSystemId", new Callable<Integer>() {
            public Integer call() throws Exception {
                return delegate.nextSystemId();
            }
        });
    }

    private <T> T time(String methodName, Callable<T> action) {
        return Timer.time(CodeListManager.class, methodName, action);
    }

    private void time(String methodName, Runnable action) {
        Timer.time(CodeListManager.class, methodName, action);
    }

}
