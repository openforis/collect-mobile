package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.util.persistence.ConnectionCallback;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.android.viewmodel.UITaxonVernacularName;
import org.openforis.collect.android.viewmodel.UiTaxon;
import org.openforis.collect.android.viewmodelmanager.TaxonService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Daniel Wiell
 */
public class TaxonRepository implements TaxonService {
    private final Database database;
    private final Map<String, Integer> taxonomyIdByName = new HashMap<String, Integer>();

    public TaxonRepository(Database database) {
        this.database = database;
        initTaxonomy();
    }

    public List<UiTaxon> find(final String query, final String taxonomy, final int maxResults) {
        return database.execute(new ConnectionCallback<List<UiTaxon>>() {
            public List<UiTaxon> execute(Connection connection) throws SQLException {
                ConstraintBuilder constraintBuilder = new ConstraintBuilder(query);
                PreparedStatement ps = connection.prepareStatement("" +
                        "SELECT taxonomy_id, code, scientific_name, vernacular_name, language_code\n" +
                        "FROM ofc_taxon t\n" +
                        "LEFT OUTER JOIN ofc_taxon_vernacular_name v ON t.id = v.taxon_id\n" +
                        "WHERE taxonomy_id = ? AND code IS NOT NULL\n" +
                        "AND " + constraintBuilder.constraint() + "\n" +
                        "ORDER BY scientific_name, vernacular_name, language_code\n" +
                        "LIMIT ?");
                ps.setInt(1, taxonomyId(taxonomy));
                int i = 1;
                for (String param : constraintBuilder.params) {
                    i += 1;
                    ps.setString(i, param);
                }
                ps.setInt(i + 1, maxResults * 3);
                ResultSet rs = ps.executeQuery();

                List<UiTaxon> result = new ArrayList<UiTaxon>();
                String lastCode = null;
                String code = null;
                String scientificName = null;
                UITaxonVernacularName vernacularName = null;
                String vernacularNameName = null;
                String languageCode = null;
                while (rs.next()) {
                    code = rs.getString("code");
                    scientificName = rs.getString("scientific_name");
                    if (lastCode == null || !lastCode.equals(code)) {
                        // always add the taxon without vernacular name
                        result.add(new UiTaxon(code, scientificName));
                    }
                    vernacularNameName = rs.getString("vernacular_name");
                    if (vernacularNameName != null) {
                        languageCode = rs.getString("language_code");
                        vernacularName = new UITaxonVernacularName(vernacularNameName, languageCode);
                        result.add(new UiTaxon(code, scientificName, vernacularName));
                    }
                    lastCode = code;
                }

                ps.close();
                rs.close();
                return result.subList(0, Math.min(maxResults, result.size()));
            }
        });
    }

    public Map<String, String> commonNameByLanguage(final String taxonCode, final String taxonomy) {
        return database.execute(new ConnectionCallback<Map<String, String>>() {
            public Map<String, String> execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement("" +
                        "SELECT language_code, vernacular_name\n" +
                        "FROM ofc_taxon t\n" +
                        "JOIN ofc_taxon_vernacular_name v ON t.id = v.taxon_id\n" +
                        "WHERE code = ? AND taxonomy_id = ?\n" +
                        "ORDER BY language_code, vernacular_name");
                ps.setString(1, taxonCode);
                ps.setInt(2, taxonomyId(taxonomy));
                ResultSet rs = ps.executeQuery();
                Map<String, String> nameByLanguage = new HashMap<String, String>();
                while (rs.next())
                    nameByLanguage.put(rs.getString("language_code"), rs.getString("vernacular_name"));
                ps.close();
                rs.close();
                return nameByLanguage;
            }
        });
    }

    private int taxonomyId(String taxonomy) {
        Integer id = taxonomyIdByName.get(taxonomy);
        if (id == null)
            throw new IllegalStateException("Unexpected taxonomy: " + taxonomy);
        return id;
    }

    private void initTaxonomy() {
        database.execute(new ConnectionCallback<Void>() {
            public Void execute(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement("SELECT id, name FROM ofc_taxonomy");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    String name = rs.getString(2);
                    taxonomyIdByName.put(name, id);
                }
                rs.close();
                ps.close();
                return null;
            }
        });
    }


    private static class ConstraintBuilder {
        private final List<String> params = new ArrayList<String>();
        private final StringBuilder constraint = new StringBuilder();

        public ConstraintBuilder(String query) {
            String[] terms = query.toLowerCase().split(" ");
            for (int i = 0; i < terms.length; i++) {
                String queryTerm = terms[i];
                if (i != 0) constraint.append("AND ");
                constraint.append("(lower(code) LIKE ?\n")
                        .append("OR lower(scientific_name) LIKE ? OR lower(scientific_name) LIKE ? \n")
                        .append("OR lower(vernacular_name) LIKE ? OR lower(vernacular_name) LIKE ?) \n");
                params.add(queryTerm + "%");

                params.add(queryTerm + "%");
                params.add("% " + queryTerm + "%");

                params.add(queryTerm + "%");
                params.add("% " + queryTerm + "%");
            }
        }

        public String constraint() {
            return "(" + constraint + ")";
        }
    }
}
