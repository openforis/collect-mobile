package org.openforis.collect.android.gui.input;

import android.content.Intent;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;

import org.openforis.collect.R;
import org.openforis.collect.android.SurveyService;
import org.openforis.collect.android.gui.SurveyNodeActivity;
import org.openforis.collect.android.gui.util.AndroidFiles;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.util.Permissions;
import org.openforis.collect.android.viewmodel.UiFileAttribute;

public class VideoFileAttributeComponent extends ImageFileAttributeComponent {

    public VideoFileAttributeComponent(UiFileAttribute attribute, SurveyService surveyService, FragmentActivity context) {
        super(attribute, surveyService, context);
    }

    @Override
    protected boolean canCapture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        return takePictureIntent.resolveActivity(context.getPackageManager()) != null;
    }

    @Override
    protected void capture() {
        if (Permissions.checkCameraPermissionOrRequestIt(context)) {
            //TODO find nicer solution to prevent FileUriExposedException
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            ((SurveyNodeActivity) context).setVideoChangedListener(this);
            Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
            // TODO: Check out http://stackoverflow.com/questions/1910608/android-action-image-capture-intent
            //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
            context.startActivityForResult(intent, SurveyNodeActivity.VIDEO_CAPTURE_REQUEST_CODE);
        }
    }

    @Override
    protected String getMediaType() {
        return "video/*";
    }

    @Override
    protected Bitmap getFileThumbnail() {
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(file.getAbsolutePath(),
                MediaStore.Video.Thumbnails.FULL_SCREEN_KIND);
        return bitmap == null ? null : scaleToFit(bitmap);
    }

    @Override
    protected void showGallery() {
        if (Permissions.checkReadExternalStoragePermissionOrRequestIt(context)) {
            ((SurveyNodeActivity) context).setVideoChangedListener(this);
            startFileChooserActivity("Select video", SurveyNodeActivity.VIDEO_SELECTED_REQUEST_CODE, getMediaType());
        }
    }

    public void videoCaptured(Uri uri) {
        if (AndroidFiles.copyUriContentToFile(context, uri, file)) {
            fileChanged();
        } else {
            Dialogs.alert(context, R.string.warning, R.string.file_attribute_capture_video_error);
        }
    }

    public void videoSelected(Uri uri) {
        videoCaptured(uri);
    }
}
