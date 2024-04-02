package org.openforis.collect.android.gui.input;

import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;

import androidx.fragment.app.FragmentActivity;

import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.util.Activities;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.viewmodel.UiFileAttribute;

import java.io.File;

public abstract class FileAttributeComponent extends AttributeComponent<UiFileAttribute> {

    protected File file;
    private boolean fileChanged;

    public FileAttributeComponent(UiFileAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        file = surveyService.file(attribute);
        file.getParentFile().mkdirs();
    }

    @Override
    public boolean hasChanged() {
        return fileChanged;
    }

    @Override
    protected boolean updateAttributeIfChanged() {
        if (hasChanged()) {
            attribute.setFile(file);
            fileChanged = false;
            return true;
        } else {
            return false;
        }
    }

    protected void fileChanged() {
        fileChanged = true;
        AndroidFiles.makeDiscoverable(file, context);
        saveNode();
        updateViewState();
    }

    protected void removeFile() {
        file.delete();
        fileChanged();
    }

    protected void startFileChooserActivity(String title, int requestCode, String type, String... extraMimeTypes) {
        Activities.startFileChooserActivity(context, title, requestCode, type, extraMimeTypes);
    }

    protected boolean canStartFileChooserActivity(String type) {
        return Activities.canStartFileChooserActivity(context, type);
    }

    protected void startShowFileActivity() {
        //TODO find nicer solution to prevent FileUriExposedException
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri fileUri = Uri.fromFile(file);
        intent.setDataAndType(fileUri, getMediaType());
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, "View using"));
    }

    protected abstract void updateViewState();

    protected abstract String getMediaType();

}
