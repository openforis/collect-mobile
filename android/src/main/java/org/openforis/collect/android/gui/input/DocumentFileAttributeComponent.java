package org.openforis.collect.android.gui.input;

import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.Files;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.util.Permissions;
import org.openforis.collect.android.viewmodel.UiFileAttribute;

import java.io.File;

public class DocumentFileAttributeComponent extends FileAttributeComponent {

    private final View inputView;
    private Button galleryButton;
    private Button viewSelectedFileButton;
    private ImageButton removeButton;

    DocumentFileAttributeComponent(UiFileAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);

        inputView = context.getLayoutInflater().inflate(R.layout.file_attribute_document, null);

        setupUiComponents();

        updateViewState();
    }

    private void setupUiComponents() {
        setupGalleryButton();
        setupFileNameView();
        setupRemoveButton();
    }

    @Override
    protected View toInputView() {
        return inputView;
    }

    protected String getMediaType() {
        return "*/*";
    }

    private void setupFileNameView() {
        viewSelectedFileButton = inputView.findViewById(R.id.file_attribute_file_selected);
        viewSelectedFileButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startShowFileActivity();
            }
        });
    }

    private void setupRemoveButton() {
        removeButton = inputView.findViewById(R.id.file_attribute_remove);
        removeButton.setImageDrawable(new Attrs(context).drawable(R.attr.deleteIcon));
        removeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showRemoveDialog();
            }
        });
    }

    private void showRemoveDialog() {
        Dialogs.confirm(context, R.string.delete, R.string.file_attribute_document_delete_confirm_message, new Runnable() {
            public void run() {
                removeFile();
            }
        });
    }

    private void setupGalleryButton() {
        galleryButton = inputView.findViewById(R.id.file_attribute_select);
        if (canShowGallery()) {
            galleryButton.setCompoundDrawablesWithIntrinsicBounds(null, new Attrs(context).drawable(R.attr.openIcon), null, null);
            galleryButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    showGallery();
                }
            });
        } else {
            galleryButton.setVisibility(View.GONE);
        }
    }

    private boolean canShowGallery() {
        return canStartFileChooserActivity(getMediaType());
    }

    protected void showGallery() {
        if (Permissions.checkReadExternalStoragePermissionOrRequestIt(context)) {
            ((SurveyNodeActivity) context).setFileDocumentChangeListener(this);
            startFileChooserActivity("Select document", SurveyNodeActivity.FILE_DOCUMENT_SELECTED_REQUEST_CODE, getMediaType());
        }
    }

    public void documentSelected(Uri uri) {
        try {
            File selectedFile = AndroidFiles.copyUriContentToCache(context, uri);
            if (selectedFile != null) {
                String extension = FilenameUtils.getExtension(selectedFile.getName());
                file = Files.changeExtension(file, extension);
                FileUtils.copyFile(selectedFile, file);
                fileChanged();
            }
        } catch (Exception e) {
            Toast.makeText(context, context.getString(R.string.file_attribute_file_select_error, e.getMessage()), Toast.LENGTH_LONG).show();
        }
    }

    protected void updateViewState() {
        Views.toggleVisibility(viewSelectedFileButton, file.exists());
        boolean canEdit = !isRecordEditLocked();
        Views.toggleVisibility(galleryButton, canEdit);
        Views.toggleVisibility(removeButton, file.exists() && canEdit);
    }

    @Override
    protected void updateEditableState() {
        updateViewState();
    }
}
