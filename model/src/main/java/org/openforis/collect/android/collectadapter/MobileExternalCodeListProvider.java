package org.openforis.collect.android.collectadapter;

import org.apache.commons.lang3.StringUtils;
import org.openforis.collect.android.util.NaturalOrderComparator;
import org.openforis.collect.android.util.persistence.ConnectionCallback;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.DatabaseExternalCodeListProvider;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeList;
import org.openforis.idm.metamodel.CodeListLevel;
import org.openforis.idm.metamodel.ExternalCodeListItem;
import org.openforis.idm.metamodel.ReferenceDataSchema;
import org.openforis.idm.model.CodeAttribute;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
public class MobileExternalCodeListProvider extends DatabaseExternalCodeListProvider {
    private final Database database;

    public MobileExternalCodeListProvider(Database database) {
        this.database = database;
    }

    @Override
    public int countRootItems(final CodeList codeList) {
        return database.execute(new ConnectionCallback<Integer>() {
            public Integer execute(Connection connection) throws SQLException {
                String query = rootItemsQuery(codeList, true);
                PreparedStatement ps = connection.prepareStatement(query);
                return executeForCount(ps);
            }
        });
    }

    public List<ExternalCodeListItem> getRootItems(final CodeList codeList) {
        return database.execute(new ConnectionCallback<List<ExternalCodeListItem>>() {
            public List<ExternalCodeListItem> execute(Connection connection) throws SQLException {
                String query = rootItemsQuery(codeList, false);
                PreparedStatement ps = connection.prepareStatement(query);
                ResultSet rs = ps.executeQuery();
                int levelIndex = 1;
                List<ExternalCodeListItem> items = parseRows(rs, codeList, levelIndex);
                rs.close();
                ps.close();
                final Comparator<String> naturalOrderComparator =
                        new NaturalOrderComparator<String>();
                Collections.sort(items, new Comparator<ExternalCodeListItem>() {
                    public int compare(ExternalCodeListItem o1, ExternalCodeListItem o2) {
                        return naturalOrderComparator.compare(o1.getCode(), o2.getCode());
                    }
                });
                return items;
            }
        });
    }

    private boolean hasSamplingPointDataLabelsInInfo(final CollectSurvey survey) {
        ReferenceDataSchema.SamplingPointDefinition samplingPointDefinition = survey.getReferenceDataSchema().getSamplingPointDefinition();
        List<String> infoAttributeNames = samplingPointDefinition.getAttributeNames();
        for (String languageCode : survey.getLanguages()) {
            int infoAttributeIndex = infoAttributeNames.indexOf("label_" + languageCode);
            if (infoAttributeIndex >= 0) {
                return true;
            }
        }
        return false;
    }

    private void setSamplingPointItemLabels(final CollectSurvey survey, final Map<String, String> row, final ExternalCodeListItem item) {
        ReferenceDataSchema.SamplingPointDefinition samplingPointDefinition = survey.getReferenceDataSchema().getSamplingPointDefinition();
        List<String> infoAttributeNames = samplingPointDefinition.getAttributeNames();
        for (String languageCode : survey.getLanguages()) {
            int infoAttributeIndex = infoAttributeNames.indexOf("label_" + languageCode);
            if (infoAttributeIndex >= 0) {
                String label = row.get("info" + (infoAttributeIndex + 1));
                if (label != null) {
                    item.setLabel(languageCode, label);
                }
            }
        }
    }

    private List<ExternalCodeListItem> parseRows(final ResultSet rs, final CodeList codeList, final int levelIndex) throws SQLException {
        List<ExternalCodeListItem> items = new ArrayList<ExternalCodeListItem>();
        CollectSurvey survey = codeList.getSurvey();
        boolean isSamplingDesignCodeListWithLabelsInInfo = isSamplingDesignCodeList(codeList)
                && hasSamplingPointDataLabelsInInfo(survey);
        while (rs.next()) {
            Map<String, String> row = toRow(rs);
            ExternalCodeListItem item = parseRow(row, codeList, levelIndex);
            if (isSamplingDesignCodeListWithLabelsInInfo) {
                setSamplingPointItemLabels(survey, row, item);
            }
            items.add(item);
        }
        return items;
    }

    private String rootItemsQuery(CodeList codeList, boolean selectCount) {
        String constraint = "1 = 1";
        int childLevel = 2; // Level is 1 based, so children of root is at level 2
        if (hasLevel(codeList, childLevel))
            constraint = levelName(codeList, childLevel) + " IS NULL";
        return "SELECT " + (selectCount ? "COUNT(*)" : "*") + "\n" +
                "FROM " + codeList.getLookupTable() + "\n" +
                "WHERE " + constraint;
    }

    public List<ExternalCodeListItem> getChildItems(final ExternalCodeListItem parentItem) {
        final CodeList codeList = parentItem.getCodeList();
        if (codeList.getHierarchy().size() <= parentItem.getLevel()) {
            return Collections.emptyList();
        }
        return database.execute(new ConnectionCallback<List<ExternalCodeListItem>>() {
            public List<ExternalCodeListItem> execute(Connection connection) throws SQLException {
                String query = childItemsQuery(parentItem, false);
                PreparedStatement ps = connection.prepareStatement(query);

                addAncestorLevelsCondition(ps, parentItem);

                ResultSet rs = ps.executeQuery();

                int levelIndex = parentItem.getLevel() + 1;
                List<ExternalCodeListItem> items = parseRows(rs, codeList, levelIndex);

                rs.close();
                ps.close();
                Collections.sort(items, new NaturalOrderComparator<ExternalCodeListItem>());
                return items;
            }
        });
    }

    @Override
    public boolean hasChildItems(final ExternalCodeListItem parentItem) {
        final CodeList codeList = parentItem.getCodeList();
        if (codeList.getHierarchy().size() <= parentItem.getLevel()) {
            return false;
        }
        return database.execute(new ConnectionCallback<Boolean>() {
            public Boolean execute(Connection connection) throws SQLException {
                String query = childItemsQuery(parentItem, false);
                PreparedStatement ps = connection.prepareStatement(query);

                addAncestorLevelsCondition(ps, parentItem);

                return executeForCount(ps) > 0;
            }
        });
    }

    public ExternalCodeListItem getItem(final CodeAttribute attribute) {
        if (attribute.getValue().getCode() == null)
            return null;
        return database.execute(new ConnectionCallback<ExternalCodeListItem>() {
            public ExternalCodeListItem execute(Connection connection) throws SQLException {
                CodeAttributeDefinition definition = attribute.getDefinition();
                CodeList codeList = definition.getList();
                String query = singleItemQuery(attribute, codeList);
                PreparedStatement ps = connection.prepareStatement(query);
                setItemQueryParams(ps, attribute);
                ResultSet rs = ps.executeQuery();

                int levelIndex = definition.getLevelPosition();
                List<ExternalCodeListItem> items = parseRows(rs, codeList, levelIndex);
                ExternalCodeListItem item = items.isEmpty() ? null :  items.get(0);

                rs.close();
                ps.close();
                return item;
            }
        });

    }

    private void setItemQueryParams(PreparedStatement ps, CodeAttribute attribute) throws SQLException {
        CodeAttribute a = attribute;
        int i = 1;
        while (a != null) {
            String code = a.getValue().getCode();
            if (code != null) {
                ps.setString(i, code);
                i++;
            }
            a = a.getCodeParent();
        }
    }

    private String singleItemQuery(CodeAttribute attribute, CodeList codeList) {
        StringBuilder constraints = new StringBuilder();
        appendSingleItemQueryConstraint(attribute, codeList, constraints);
        return "SELECT *\n" +
                "FROM " + codeList.getLookupTable() + "\n" +
                "WHERE 1 = 1" + constraints;
    }

    private void appendSingleItemQueryConstraint(CodeAttribute attribute, CodeList codeList, StringBuilder s) {
        int currentLevel = attribute.getDefinition().getLevelPosition();
        CodeAttribute currentAttribute = attribute;
        // append current level and ancestors conditions
        while(currentLevel > 0 && currentAttribute != null) {
            String levelName = levelName(codeList, currentLevel);
            s.append(" AND ").append(levelName);
            if (currentAttribute.getValue().getCode() == null) {
                s.append(" IS NULL");
            } else {
                s.append(" = ?");
            }
            currentAttribute = attribute.getCodeParent();
            currentLevel --;
        }
        // append following levels null conditions
        int currentNextLevel = attribute.getDefinition().getLevelPosition() + 1;
        while (currentNextLevel <= codeList.getHierarchy().size()) {
            String levelName = levelName(codeList, currentNextLevel);
            s.append(" AND ").append(levelName).append(" IS NULL");
            currentNextLevel ++;
        }
    }

    private String childItemsQuery(ExternalCodeListItem parentItem, boolean selectCount) {
        CodeList codeList = parentItem.getCodeList();
        List<CodeListLevel> hierarchy = codeList.getHierarchy();

        List<String> constraints = new ArrayList<String>();

        // ancestor levels
        for (CodeListLevel ancestorLevel: getAncestorLevels(parentItem)) {
            constraints.add(ancestorLevel.getName() + " = ?");
        }

        // current level
        String childName = levelName(codeList, parentItem.getLevel() + 1);
        constraints.add(childName + " IS NOT NULL");

        // descendant levels
        List<CodeListLevel> descendantLevels = hierarchy.subList(parentItem.getLevel() + 1, hierarchy.size());
        for (CodeListLevel descendantLevel : descendantLevels) {
            constraints.add(descendantLevel.getName() + " IS NULL");
        }

        return String.format("SELECT " + (selectCount ? "COUNT(*)" : "*") + "\n" +
                "FROM %s \n" +
                "WHERE %s", codeList.getLookupTable(), StringUtils.join(constraints, " AND "));
    }

    private String levelName(CodeList codeList, int level) {
        if (level > codeList.getHierarchy().size())
            return "NULL";
        return codeList.getHierarchy().get(level - 1).getName();
    }

    private boolean hasLevel(CodeList codeList, int level) {
        return codeList.getHierarchy().size() >= level;
    }

    private List<CodeListLevel> getAncestorLevels(ExternalCodeListItem item) {
        return item.getCodeList().getHierarchy().subList(0, item.getLevel());
    }

    private void addAncestorLevelsCondition(PreparedStatement ps, ExternalCodeListItem parentItem) throws SQLException {
        List<CodeListLevel> ancestorLevels = getAncestorLevels(parentItem);
        for (int level = 1; level <= ancestorLevels.size(); level ++) {
            String ancestorCode = level == parentItem.getLevel()
                    ? parentItem.getCode()
                    : parentItem.getParentKeyByLevel().get(ancestorLevels.get(level - 1).getName());
            ps.setString(level, ancestorCode);
        }
    }

    public String getCode(CodeList codeList, String s, Object... objects) {
        throw new UnsupportedOperationException("Not implemented - deprecated");
    }

    private Map<String, String> toRow(ResultSet rs) throws SQLException {
        HashMap<String, String> row = new HashMap<String, String>();
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i < metaData.getColumnCount(); i++)
            row.put(metaData.getColumnName(i).toLowerCase(), rs.getString(i));
        return row;
    }

    private int executeForCount(PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.executeQuery();
        int count = 0;
        if (rs.next())
            count = rs.getInt(1);
        rs.close();
        ps.close();
        return count;
    }

    private boolean isSamplingDesignCodeList(CodeList list) {
        return ((CollectSurvey) list.getSurvey()).isSamplingDesignCodeList(list);
    }
}
