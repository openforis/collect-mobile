package org.openforis.collect.android.gui.input;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import org.apache.commons.io.FileUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.collect.android.gui.util.Bitmaps;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.util.Permissions;
import org.openforis.collect.android.viewmodel.UIFileAttributeDefinition;
import org.openforis.collect.android.viewmodel.UiFileAttribute;

import java.io.File;
import java.io.IOException;

public class ImageFileAttributeComponent extends FileAttributeComponent {

    private static final int MAX_DISPLAY_WIDTH = 375;
    private static final int MAX_DISPLAY_HEIGHT = 500;

    private final View inputView;
    private Button captureButton;
    private Button galleryButton;
    private ImageButton removeButton;
    private ImageButton rotateImageButton;
    private ImageView thumbnailImageView;
    private String tempImagePath;

    ImageFileAttributeComponent(UiFileAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);

        inputView = context.getLayoutInflater().inflate(R.layout.file_attribute_image, null);

        setupUiComponents();

        updateViewState();
    }

    private void setupUiComponents() {
        setupImageView();
        setupCaptureButton();
        setupGalleryButton();
        setupRotateButton();
        setupRemoveButton();
    }

    private void setupImageView() {
        thumbnailImageView = inputView.findViewById(R.id.file_attribute_image);
        thumbnailImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startShowFileActivity();
            }
        });
    }

    @Override
    protected View toInputView() {
        return inputView;
    }

    @Override
    protected String getMediaType() {
        return "image/*";
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
        Dialogs.confirm(context, R.string.delete, R.string.file_attribute_image_delete_confirm_message, new Runnable() {
            public void run() {
                removeFile();
            }
        });
    }

    private void setupRotateButton() {
        rotateImageButton = inputView.findViewById(R.id.file_attribute_rotate);
        rotateImageButton.setImageDrawable(new Attrs(context).drawable(R.attr.rotateIcon));
        rotateImageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                rotateImage();
            }
        });
    }

    private void rotateImage() {
        Bitmaps.rotateFile(file);
        fileChanged();
    }

    private void setupCaptureButton() {
        captureButton = inputView.findViewById(R.id.file_attribute_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Runnable captureImageRunnable = new Runnable() {
                    public void run() {
                        capture();
                    }
                };
                if (file != null && file.exists()) {
                    Dialogs.confirm(context, R.string.warning, R.string.file_attribute_captured_file_overwrite_confirm_message,
                            captureImageRunnable, null, R.string.overwrite_label, android.R.string.cancel);
                } else {
                    captureImageRunnable.run();
                }
            }
        });
    }

    protected void capture() {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (Permissions.checkCameraPermissionOrRequestIt(context)) {
                ((SurveyNodeActivity) context).setImageChangedListener(this);
                Uri imageUri;
                if (AndroidVersion.greaterThan20()) {
                    // Create temp file and store image there
                    File imageFile = createTempImageFile();
                    if (imageFile == null) {
                        Toast.makeText(context, R.string.file_attribute_capture_image_error_creating_temp_file, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    imageUri = AndroidFiles.getUriForFile(context, imageFile);
                } else {
                    // Store image directly to "file"
                    //TODO find nicer solution to prevent FileUriExposedException
                    StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
                    StrictMode.setVmPolicy(builder.build());
                    imageUri = Uri.fromFile(file);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                context.startActivityForResult(takePictureIntent, SurveyNodeActivity.IMAGE_CAPTURE_REQUEST_CODE);
            }
        } catch (Exception e) {
            String errorMessage = context.getString(R.string.file_attribute_capture_image_error, e.getMessage());
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
        }
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
            ((SurveyNodeActivity) context).setImageChangedListener(this);
            startFileChooserActivity("Select image", SurveyNodeActivity.IMAGE_SELECTED_REQUEST_CODE, getMediaType());
        }
    }

    public void imageCaptured() {
        if (AndroidVersion.greaterThan20()) {
            // Copy image from temp file
            File tempFile = null;
            try {
                tempFile = new File(tempImagePath);
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(tempFile));
                saveImageIntoFile(bitmap);
            } catch (IOException e) {
                Dialogs.alert(context, context.getString(R.string.warning),
                        context.getString(R.string.file_attribute_capture_image_error, e.getMessage()));
            } finally {
                tempImagePath = null;
                FileUtils.deleteQuietly(tempFile);
            }
        } else {
            // Attribute file already passed to camera intent
            fileChanged();
        }
    }

    public void imageSelected(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            saveImageIntoFile(bitmap);
        } catch (IOException e) {
            Dialogs.alert(context, context.getString(R.string.warning),
                    context.getString(R.string.file_attribute_select_image_error, e.getMessage()));
        }
    }

    private void saveImageIntoFile(Bitmap bitmap) throws IOException {
        int maxSize = ((UIFileAttributeDefinition) attribute.getDefinition()).getMaxSize();
        if (Bitmaps.saveToFile(bitmap, file, Bitmap.CompressFormat.JPEG, maxSize)) {
            fileChanged();
        }
    }

    private void showThumbnail() {
        Views.show(thumbnailImageView);
        Bitmap bitmap = getFileThumbnail();
        thumbnailImageView.setImageBitmap(bitmap);
    }

    private void hideThumbnail() {
        Views.hide(thumbnailImageView);
    }

    protected Bitmap getFileThumbnail() {
        return Bitmaps.scaleToFit(file.getAbsolutePath(), MAX_DISPLAY_WIDTH, MAX_DISPLAY_HEIGHT);
    }

    Bitmap scaleToFit(Bitmap bitmap) {
        return Bitmaps.scaleToFit(bitmap, MAX_DISPLAY_WIDTH, MAX_DISPLAY_HEIGHT);
    }

    protected void updateViewState() {
        if (file.exists()) {
            showThumbnail();
        } else {
            hideThumbnail();
        }
        boolean canAddNew = !isRecordEditLocked();
        Views.toggleVisibility(captureButton, canAddNew);
        Views.toggleVisibility(galleryButton, canAddNew);

        boolean canDeleteOrModify = file.exists() && !isRecordEditLocked();
        Views.toggleVisibility(removeButton, canDeleteOrModify);
        Views.toggleVisibility(rotateImageButton, canDeleteOrModify);
    }

    private File createTempImageFile() {
        try {
            // Create an image file name
            File storageDir = context.getCacheDir();
            File image = File.createTempFile("temp_image_", ".jpg", storageDir );
            // Save a file: path for use with ACTION_VIEW intents
            tempImagePath = image.getAbsolutePath();
            return image;
        } catch (IOException e) {
            Dialogs.alert(context, context.getString(R.string.warning),
                    context.getString(R.string.file_attribute_capture_image_error, e.getMessage()));
            return null;
        }
    }

    @Override
    protected void updateEditableState() {
        updateViewState();
    }
}
