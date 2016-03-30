package org.openforis.collect.android.gui;

import org.openforis.collect.android.gui.util.meter.Timer;
import org.openforis.collect.android.sqlite.AndroidDatabase;
import org.openforis.collect.android.util.persistence.ConnectionCallback;
import org.openforis.collect.android.util.persistence.PreparedStatementHelper;
import org.openforis.collect.persistence.CodeListItemDao;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.PersistedCodeListItem;
import org.openforis.idm.metamodel.Survey;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Callable;

// TODO: Rewrite this using DataSource
public class MobileCodeListItemDao extends CodeListItemDao {
    private final AndroidDatabase database;
    private final ChildCodeListItemsRepository childCodeListItemsRepository;
    private final CodeListItemRepository codeListItemRepository;

    public MobileCodeListItemDao(AndroidDatabase database) {
        super();
        this.database = database;
        childCodeListItemsRepository = new ChildCodeListItemsRepository(database);
        codeListItemRepository = new CodeListItemRepository(database);
    }

    @Override
    protected List<PersistedCodeListItem> loadChildItems(CodeList codeList, Integer parentItemId, ModelVersion version) {
        return childCodeListItemsRepository.load(codeList, parentItemId);
    }

    public PersistedCodeListItem loadItem(final CodeList codeList, final Integer parentItemId, final String code, final ModelVersion version) {
        return codeListItemRepository.load(codeList, parentItemId, code);
    }

    public PersistedCodeListItem loadItem(final CodeList codeList, final String code, final ModelVersion version) {
        return codeListItemRepository.load(codeList, null, code);
    }

    /**
     * Inserts the items in batch.
     */
    public void insert(final List<PersistedCodeListItem> items) {
        time("insert", new Runnable() {
            public void run() {
                database.execute(new ConnectionCallback<Void>() {
                    public Void execute(Connection connection) throws SQLException {
                        PersistedCodeListItem firstItem = items.get(0);
                        int surveyId = firstItem.getSurvey().getId();
                        int nextSystemId = maxSystemId(connection) + 1;
                        PreparedStatement ps = connection.prepareStatement("INSERT INTO ofc_code_list(\n" +
                                "id, survey_id, code_list_id, item_id, parent_id, sort_order, code, qualifiable, since_version_id, " +
                                "deprecated_version_id, label1, label2, label3, description1, description2, description3)" +
                                "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                        for (PersistedCodeListItem item : items) {
                            if (item.getSystemId() == null)
                                item.setSystemId(nextSystemId);
                            nextSystemId = Math.max(item.getSystemId(), nextSystemId) + 1;
                            PreparedStatementHelper psh = new PreparedStatementHelper(ps);
                            psh.setInt(item.getSystemId());
                            psh.setInt(surveyId);
                            psh.setInt(item.getCodeList().getId());
                            psh.setInt(item.getId());
                            psh.setIntOrNull(item.getParentId());
                            psh.setIntOrNull(item.getSortOrder());
                            psh.setString(item.getCode());
                            psh.setBoolean(item.isQualifiable());
                            psh.setStringOrNull(item.getSinceVersionName());
                            psh.setStringOrNull(item.getDeprecatedVersionName());
                            for (String label : labels(item))
                                psh.setStringOrNull(label);
                            for (String description : descriptions(item))
                                psh.setStringOrNull(description);
                            ps.addBatch();
                        }

                        ps.executeBatch();
                        return null;
                    }
                });
            }
        });
    }

    private String[] labels(PersistedCodeListItem item) {
        int size = 3; // Three labels are persisted
        String[] result = new String[size];
        Survey survey = item.getSurvey();
        List<String> languages = survey.getLanguages();
        for (int i = 0; i < size; i++) {
            String label;
            if (i < languages.size()) {
                String lang = languages.get(i);
                label = item.getLabel(lang);
            } else {
                label = null;
            }
            result[i] = label;
        }
        return result;
    }

    private String[] descriptions(PersistedCodeListItem item) {
        int size = 3; // Three descriptions are persisted
        String[] result = new String[size];
        Survey survey = item.getSurvey();
        List<String> languages = survey.getLanguages();
        for (int i = 0; i < size; i++) {
            String description;
            if (i < languages.size()) {
                String lang = languages.get(i);
                description = item.getDescription(lang);
            } else {
                description = null;
            }
            result[i] = description;
        }
        return result;
    }

    private int maxSystemId(Connection connection) throws SQLException {
        ResultSet rs = connection.createStatement().executeQuery("SELECT MAX(id) id FROM ofc_code_list");
        rs.next();
        int maxId = rs.getInt("id");
        rs.close();
        return maxId;
    }

    private <T> T time(String methodName, Callable<T> action) {
        return Timer.time(MobileCodeListItemDao.class, methodName, action);
    }

    private void time(String methodName, Runnable action) {
        Timer.time(MobileCodeListItemDao.class, methodName, action);
    }

}
