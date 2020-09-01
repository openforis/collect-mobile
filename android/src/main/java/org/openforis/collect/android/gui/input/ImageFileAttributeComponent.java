package org.openforis.collect.android.gui.input;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.Views;
import org.openforis.collect.android.util.Permissions;
import org.openforis.collect.android.viewmodel.UiFileAttribute;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageFileAttributeComponent extends FileAttributeComponent {

    private static final int MAX_DISPLAY_WIDTH = 375;
    private static final int MAX_DISPLAY_HEIGHT = 500;

    private View inputView;
    private Button removeButton;
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
        setupRemoveButton();
    }

    private void setupImageView() {
        thumbnailImageView = inputView.findViewById(R.id.file_attribute_image);
        thumbnailImageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startShowImageActivity();
            }
        });
    }

    private void startShowImageActivity() {
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

    @Override
    protected View toInputView() {
        return inputView;
    }

    protected String getMediaType() {
        return "image/*";
    }

    private void setupRemoveButton() {
        removeButton = inputView.findViewById(R.id.file_attribute_remove);
        removeButton.setCompoundDrawablesWithIntrinsicBounds(new Attrs(context).drawable(R.attr.deleteIcon), null, null, null);
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

    private void setupCaptureButton() {
        Button button = inputView.findViewById(R.id.file_attribute_capture);
        if (canCapture()) {
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    Runnable captureImageRunnable = new Runnable() {
                        public void run() {
                            capture();
                        }
                    };
                    if (file != null && file.exists()) {
                        Dialogs.confirm(context, R.string.warning, R.string.file_attribute_captured_file_overwrite_confirm_message,
                                captureImageRunnable, null, R.string.overwrite_label, R.string.cancel_label);
                    } else {
                        captureImageRunnable.run();
                    }
                }
            });
        } else {
            button.setVisibility(View.GONE);
        }
    }

    protected boolean canCapture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        return takePictureIntent.resolveActivity(context.getPackageManager()) != null;
    }

    protected void capture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null &&
                Permissions.checkCameraPermissionOrRequestIt(context)) {
            ((SurveyNodeActivity) context).setImageChangedListener(this);
            Uri imageUri;
            if (AndroidVersion.greaterThan20()) {
                // Create temp file and store image there
                File imageFile = createTempImageFile();
                if (imageFile == null) {
                    Toast.makeText(context, R.string.file_attribute_capture_image_error_creating_temp_file, Toast.LENGTH_SHORT);
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
    }

    private void setupGalleryButton() {
        Button button = inputView.findViewById(R.id.file_attribute_select);
        if (canShowGallery()) {
            button.setCompoundDrawablesWithIntrinsicBounds(new Attrs(context).drawable(R.attr.openIcon), null, null, null);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    showGallery();
                }
            });
        } else {
            button.setVisibility(View.GONE);
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
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fo);
            fileChanged();
        } finally {
            IOUtils.closeQuietly(fo);
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
        return resizeImage(file.getAbsolutePath(), MAX_DISPLAY_WIDTH, MAX_DISPLAY_HEIGHT);
    }

    private Bitmap resizeImage(String filePath, int maxWidth, int maxHeight) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bounds);
        int width = bounds.outWidth;
        int height = bounds.outHeight;
        boolean withinBounds = width <= maxWidth && height <= maxHeight;
        BitmapFactory.Options resample = new BitmapFactory.Options();
        if (!withinBounds)
            resample.inSampleSize = (int) Math.round(Math.min((double) width / (double) maxWidth, (double) height / (double) maxHeight));
        return BitmapFactory.decodeFile(filePath, resample);
    }

    Bitmap scaleToFit(Bitmap bitmap) {
        return resizeImage(bitmap, MAX_DISPLAY_WIDTH, MAX_DISPLAY_HEIGHT);
    }

    Bitmap resizeImage(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratio = Math.min(
                (float) maxWidth / width,
                (float) maxHeight / height);
        int scaledWith = Math.round(ratio * width);
        int scaledHeight = Math.round(ratio * height);
        return Bitmap.createScaledBitmap(bitmap, scaledWith, scaledHeight, false);
    }

    protected void updateViewState() {
        if (file.exists()) {
            showThumbnail();
        } else {
            hideThumbnail();
        }
        Views.toggleVisibility(removeButton, file.exists());

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

}
