package org.openforis.collect.android.gui.input;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.util.CollectPermissions;
import org.openforis.collect.android.viewmodel.UiFileAttribute;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public abstract class FileAttributeComponent extends AttributeComponent<UiFileAttribute> {

    protected final File file;
    protected boolean fileChanged;

    public FileAttributeComponent(UiFileAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        file = surveyService.file(attribute);
        file.getParentFile().mkdirs();
    }

    @Override
    protected boolean updateAttributeIfChanged() {
        if (fileChanged) {
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
    }

    protected void removeFile() {
        file.delete();
        fileChanged = true;
        AndroidFiles.makeDiscoverable(file, context);
        saveNode();
    }

}
