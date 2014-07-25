package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.util.persistence.ConnectionCallback;
import org.openforis.collect.android.util.persistence.Database;
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
                PreparedStatement ps = connection.prepareStatement("" +
                        "SELECT taxonomy_id, code, scientific_name\n" +
                        "FROM ofc_taxon\n" +
                        "WHERE taxonomy_id = ? AND code IS NOT NULL\n" +
                        "AND (lower(code) LIKE ? OR lower(scientific_name) LIKE ?)" +
                        "ORDER BY scientific_name\n" +
                        "LIMIT ?");
                ps.setInt(1, taxonomyId(taxonomy));
                ps.setString(2, query.toLowerCase() + "%");
                ps.setString(3, query.toLowerCase() + "%");
                ps.setInt(4, maxResults);
                ResultSet rs = ps.executeQuery();
                List<UiTaxon> result = new ArrayList<UiTaxon>();
                while (rs.next())
                    result.add(toTaxon(rs));
                ps.close();
                rs.close();
                return result;
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

    private UiTaxon toTaxon(ResultSet rs) throws SQLException {
        return new UiTaxon(rs.getString("code"), rs.getString("scientific_name"));
    }
}
