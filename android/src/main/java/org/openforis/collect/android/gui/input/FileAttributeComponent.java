package org.openforis.collect.android.gui.input;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import org.apache.commons.io.FileUtils;
import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.util.Attrs;
import org.openforis.collect.android.viewmodel.UiFileAttribute;

import java.io.File;
import java.io.IOException;

public class FileAttributeComponent extends AttributeComponent<UiFileAttribute> {
    private final View inputView;
    private final File imageFile;
    private boolean imageChanged;

    public FileAttributeComponent(UiFileAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
        imageFile = surveyService.file(attribute);
        imageFile.getParentFile().mkdirs();
        inputView = context.getLayoutInflater().inflate(R.layout.file_attribute, null);

        setupCaptureButton();
        setupGalleryButton();
        setupRemoveButton();
        if (imageFile.exists())
            showImage();
    }

    private void setupRemoveButton() {
        Button button = (Button) inputView.findViewById(R.id.file_attribute_remove);
        if (imageFile.exists()) {
            button.setCompoundDrawablesWithIntrinsicBounds(new Attrs(context).drawable(R.attr.cameraIcon), null, null, null);
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    showRemoveDialog();
                }
            });
        } else {
            button.setVisibility(View.INVISIBLE);
        }
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
        ((SurveyNodeActivity) context).setImageChangedListener(this);
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // TODO: Check out http://stackoverflow.com/questions/1910608/android-action-image-capture-intent
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        context.startActivityForResult(takePictureIntent, SurveyNodeActivity.IMAGE_CAPTURE_REQUEST_CODE);
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
        ((SurveyNodeActivity) context).setImageChangedListener(this);
        Intent intent = showGalleryIntent();
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        context.startActivityForResult(Intent.createChooser(intent, "Select Image"), SurveyNodeActivity.IMAGE_SELECTED_REQUEST_CODE);
    }

    // TODO: How to trigger the deletion of images after entities are deleted?

    protected boolean updateAttributeIfChanged() {
        if (imageChanged) {
            attribute.setFile(imageFile);
            imageChanged = false;
            return true;
        }
        return false;
    }

    protected View toInputView() {
        return inputView;
    }

    public void imageSelected(Uri imageUri) {
        Cursor cursor = context.getContentResolver().query(imageUri, new String[]{android.provider.MediaStore.Images.ImageColumns.DATA}, null, null, null);
        if (cursor == null)
            return;
        try {
            cursor.moveToFirst();
            String imageFilePath = cursor.getString(0);
            FileUtils.copyFile(new File(imageFilePath), imageFile);
            imageChanged();
        } catch (IOException e) {
            throw new RuntimeException("Failed to copy image");
        } finally {
            cursor.close();
        }
    }

    public void removeImage() {
        imageFile.delete();
        imageChanged = true;
        ImageView imageView = (ImageView) inputView.findViewById(R.id.file_attribute_image);
        imageView.setVisibility(View.INVISIBLE);
        View removeButton = inputView.findViewById(R.id.file_attribute_remove);
        removeButton.setVisibility(View.INVISIBLE);
        saveNode();
    }

    public void imageChanged() {
        imageChanged = true;
        showImage();
        View removeButton = inputView.findViewById(R.id.file_attribute_remove);
        removeButton.setVisibility(View.VISIBLE);
        saveNode();
    }

    private void showImage() {
        ImageView imageView = (ImageView) inputView.findViewById(R.id.file_attribute_image);
        imageView.setVisibility(View.VISIBLE);
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
        imageView.setImageBitmap(bitmap);
    }
}
