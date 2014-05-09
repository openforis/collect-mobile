package org.openforis.collect.android.collectadapter;

import org.apache.commons.io.IOUtils;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiRecord;
import org.openforis.collect.android.viewmodel.UiRecordCollection;
import org.openforis.collect.android.viewmodel.UiSurvey;
import org.openforis.collect.io.data.BackupDataExtractor;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.persistence.xml.DataMarshaller;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Daniel Wiell
 */
public class SurveyExporter {
    private final UiSurvey survey;
    private final CollectRecordProvider collectRecordProvider;
    private final DataMarshaller dataMarshaller;
    private ZipOutputStream zipOutputStream;

    public SurveyExporter(UiSurvey survey, CollectRecordProvider collectRecordProvider) throws IOException {
        this.survey = survey;
        this.collectRecordProvider = collectRecordProvider;
        dataMarshaller = new DataMarshaller();
    }

    public void export(File outputFile) throws IOException {
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(outputFile));
            exportRecords();
        } finally {
            IOUtils.closeQuietly(zipOutputStream);
        }
    }

    private void exportRecords() throws IOException {
        for (UiNode rc : survey.getChildren()) {
            UiRecordCollection recordCollection = (UiRecordCollection) rc;
            for (UiNode rp : recordCollection.getChildren()) {
                UiRecord.Placeholder recordPlaceholder = (UiRecord.Placeholder) rp;
                CollectRecord record = collectRecordProvider.record(recordPlaceholder.getId());
                exportRecord(record);
            }
        }
    }

    private void exportRecord(CollectRecord record) throws IOException {
        BackupDataExtractor.BackupRecordEntry recordEntry = new BackupDataExtractor.BackupRecordEntry(CollectRecord.Step.ENTRY, record.getId());
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
