package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.RecordSummarySortField;
import org.openforis.collect.persistence.RecordDao;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Daniel Wiell
 */
public class MobileRecordDao extends RecordDao {
    private final Database database;

    public MobileRecordDao(Database database) {
        this.database = database;
    }

    @Transactional
    public List<CollectRecord> loadSummaries(CollectSurvey survey,
                                             String rootEntity,
                                             CollectRecord.Step step,
                                             Date modifiedSince,
                                             int offset, int maxRecords,
                                             List<RecordSummarySortField> sortFields,
                                             String... keyValues) {
        return Collections.emptyList(); // TODO: Implement...
    }
}
