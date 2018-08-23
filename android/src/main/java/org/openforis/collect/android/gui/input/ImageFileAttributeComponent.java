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

import java.io.FileOutputStream;
import java.io.IOException;

public class ImageFileAttributeComponent extends FileAttributeComponent {

    public static final int MAX_DISPLAY_WIDTH = 375;
    public static final int MAX_DISPLAY_HEIGHT = 500;

    private final View inputView;

    public ImageFileAttributeComponent(UiFileAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        inputView = context.getLayoutInflater().inflate(R.layout.file_attribute_image, null);

        setupCaptureButton();
        setupGalleryButton();
        setupRemoveButton();
        if (file.exists())
            showImage();
    }

    @Override
    protected View toInputView() {
        return inputView;
    }

    private void setupRemoveButton() {
        Button button = (Button) inputView.findViewById(R.id.file_attribute_remove);
        button.setCompoundDrawablesWithIntrinsicBounds(new Attrs(context).drawable(R.attr.cameraIcon), null, null, null);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showRemoveDialog();
            }
        });
        if (!file.exists())
            button.setVisibility(View.INVISIBLE);
    }

    private void showRemoveDialog() {
        new AlertDialog.Builder(context)
                .setTitle("Delete entry")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        removeImage();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void setupCaptureButton() {
        Button button = (Button) inputView.findViewById(R.id.file_attribute_capture);
        if (canCaptureImage()) {
            button.setCompoundDrawablesWithIntrinsicBounds(new Attrs(context).drawable(R.attr.cameraIcon), null, null, null);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    captureImage();
                }
            });
        } else {
            button.setVisibility(View.GONE);
        }
    }

    private boolean canCaptureImage() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        return takePictureIntent.resolveActivity(context.getPackageManager()) != null;
    }

    private void captureImage() {
        if (CollectPermissions.checkCameraPermissionOrRequestIt(context)) {
            //TODO find nicer solution to prevent FileUriExposedException
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            ((SurveyNodeActivity) context).setImageChangedListener(this);
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // TODO: Check out http://stackoverflow.com/questions/1910608/android-action-image-capture-intent
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            context.startActivityForResult(takePictureIntent, SurveyNodeActivity.IMAGE_CAPTURE_REQUEST_CODE);
        }
    }

    private void setupGalleryButton() {
        Button button = (Button) inputView.findViewById(R.id.file_attribute_select);
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
        Intent intent = showGalleryIntent();
        return intent.resolveActivity(context.getPackageManager()) != null;
    }

    private Intent showGalleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        return intent;
    }

    private void showGallery() {
        if (CollectPermissions.checkReadExternalStoragePermissionOrRequestIt(context)) {
            ((SurveyNodeActivity) context).setImageChangedListener(this);
            Intent intent = showGalleryIntent();
            context.startActivityForResult(Intent.createChooser(intent, "Select Image"), SurveyNodeActivity.IMAGE_SELECTED_REQUEST_CODE);
        }
    }

    public void imageCaptured(Bitmap bitmap) {
        try {
            saveImage(bitmap);
            imageChanged();
        } catch(IOException e) {
            Dialogs.alert(context, context.getString(R.string.warning),
                    context.getString(R.string.file_attribute_capture_image_error, e.getMessage()));
        }
    }

    public void imageSelected(Uri imageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imageUri);
            saveImage(bitmap);
            imageChanged();
        } catch(IOException e) {
            Dialogs.alert(context, context.getString(R.string.warning),
                    context.getString(R.string.file_attribute_select_image_error, e.getMessage()));
        }
    }

    public void saveImage(Bitmap bitmap) throws IOException {
        FileOutputStream fo = new FileOutputStream(file);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fo);
        fo.close();
    }

    public void removeImage() {
        removeFile();
        ImageView imageView = inputView.findViewById(R.id.file_attribute_image);
        imageView.setVisibility(View.INVISIBLE);
        View removeButton = inputView.findViewById(R.id.file_attribute_remove);
        removeButton.setVisibility(View.INVISIBLE);
    }

    public void imageChanged() {
        fileChanged();
        showImage();
        View removeButton = inputView.findViewById(R.id.file_attribute_remove);
        removeButton.setVisibility(View.VISIBLE);
    }

    private void showImage() {
        ImageView imageView = inputView.findViewById(R.id.file_attribute_image);
        imageView.setVisibility(View.VISIBLE);
        Bitmap bitmap = scaleImage(file.getAbsolutePath(), MAX_DISPLAY_WIDTH, MAX_DISPLAY_HEIGHT);
        imageView.setImageBitmap(bitmap);
    }

    private Bitmap scaleImage(String filePath, int maxWidth, int maxHeight) {
        BitmapFactory.Options resample = new BitmapFactory.Options();
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, bounds);
        int width = bounds.outWidth;
        int height = bounds.outHeight;
        boolean withinBounds = width <= maxWidth && height <= maxHeight;
        if (!withinBounds)
            resample.inSampleSize = (int) Math.round(Math.min((double) width / (double) maxWidth, (double) height / (double) maxHeight));
        return BitmapFactory.decodeFile(filePath, resample);
    }
}
