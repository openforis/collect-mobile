package org.openforis.collect.android.gui.input;

import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;

import androidx.fragment.app.FragmentActivity;

import org.openforis.collect.android.SurveyService;
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
        Intent intent = createFileSelectorIntent(type, extraMimeTypes);
        context.startActivityForResult(Intent.createChooser(intent, title), requestCode);
    }

    protected boolean canStartFileChooserActivity(String type) {
        Intent intent = createFileSelectorIntent(type);
        return intent.resolveActivity(context.getPackageManager()) != null;
    }

    private Intent createFileSelectorIntent(String type, String... extraMimeTypes) {
        Intent intent = new Intent();
        intent.setType(type);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        if (extraMimeTypes != null && extraMimeTypes.length > 0) {
            intent.putExtra(Intent.EXTRA_MIME_TYPES, extraMimeTypes);
        }
        return intent;
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
