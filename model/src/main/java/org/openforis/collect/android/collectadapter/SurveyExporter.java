package org.openforis.collect.android.collectadapter;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.Collect;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiRecord;
import org.openforis.collect.android.viewmodel.UiRecordCollection;
import org.openforis.collect.android.viewmodel.UiSurvey;
import org.openforis.collect.io.SurveyBackupInfo;
import org.openforis.collect.io.SurveyBackupJob;
import org.openforis.collect.io.data.BackupDataExtractor;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.persistence.xml.DataMarshaller;
import org.openforis.idm.metamodel.ModelVersion;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Daniel Wiell
 */
public class SurveyExporter {
    private final UiSurvey uiSurvey;
    private final CollectSurvey collectSurvey;
    private final SurveyManager surveyManager;
    private final CollectRecordProvider collectRecordProvider;
    private final DataMarshaller dataMarshaller;
    private ZipOutputStream zipOutputStream;

    public SurveyExporter(UiSurvey uiSurvey, CollectSurvey collectSurvey, SurveyManager surveyManager, CollectRecordProvider collectRecordProvider) throws IOException {
        this.uiSurvey = uiSurvey;
        this.collectSurvey = collectSurvey;
        this.surveyManager = surveyManager;
        this.collectRecordProvider = collectRecordProvider;
        dataMarshaller = new DataMarshaller();
    }

    public void export(File outputFile) throws IOException {
        try {
            if (!outputFile.getParentFile().exists())
                outputFile.getParentFile().mkdirs();
            zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile));
            addInfoFile();
            addIdmFile();
            exportRecords();
        } finally {
            IOUtils.closeQuietly(zipOutputStream);
        }
    }

    private void addInfoFile() throws IOException {
        try {
            zipOutputStream.putNextEntry(new ZipEntry(SurveyBackupJob.INFO_FILE_NAME));
            SurveyBackupInfo info = new SurveyBackupInfo();
            info.setSurveyName(collectSurvey.getName());
            info.setSurveyUri(collectSurvey.getUri());
            info.store(zipOutputStream);
        } finally {
            zipOutputStream.closeEntry();
        }
    }

    private void addIdmFile() throws IOException {
        try {
            zipOutputStream.putNextEntry(new ZipEntry(SurveyBackupJob.SURVEY_XML_ENTRY_NAME));
            surveyManager.marshalSurvey(collectSurvey, zipOutputStream, false, false, false);
        } finally {
            zipOutputStream.closeEntry();
        }
    }

    private void exportRecords() throws IOException {
        for (UiNode rc : uiSurvey.getChildren()) {
            UiRecordCollection recordCollection = (UiRecordCollection) rc;
            for (UiNode rp : recordCollection.getChildren()) {
                UiRecord.Placeholder recordPlaceholder = (UiRecord.Placeholder) rp;
                CollectRecord record = collectRecordProvider.record(recordPlaceholder.getId());
                exportRecord(record);
            }
        }
    }

    private void exportRecord(CollectRecord record) throws IOException {
        BackupDataExtractor.BackupRecordEntry recordEntry = new BackupDataExtractor.BackupRecordEntry(CollectRecord.Step.CLEANSING, record.getId());
        ZipEntry entry = new ZipEntry(recordEntry.getName());
        try {
            zipOutputStream.putNextEntry(entry);
            OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream);
            try {
                dataMarshaller.write(record, writer);
            } catch (XmlPullParserException e) {
                throw new IllegalStateException(e);
            }
        } finally {
            zipOutputStream.closeEntry();
        }

    }

    interface CollectRecordProvider {
        CollectRecord record(int recordId);
    }
}
