package org.openforis.collect.android.gui.barcode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.SparseArray;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;

import java.io.ByteArrayOutputStream;

public class BoxDetector extends Detector {
    private Detector mDelegate;
    //rect boundaries
    private Rect croppingRect;

    public BoxDetector(Detector delegate) {
        mDelegate = delegate;
    }

    public void setCroppingRect(Rect rect) {
        this.croppingRect = rect;
    }

    public SparseArray detect(Frame frame) {
        Bitmap bitmap = crop(frame);

        Frame croppedFrame =
                new Frame.Builder()
                        .setBitmap(bitmap)
                        .setRotation(frame.getMetadata().getRotation())
                        .build();

        return mDelegate.detect(croppedFrame);
    }

    private Bitmap crop(Frame frame) {
        int width = frame.getMetadata().getWidth();
        int height = frame.getMetadata().getHeight();

        YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(croppingRect, 100, byteArrayOutputStream);
        byte[] jpegArray = byteArrayOutputStream.toByteArray();
        Bitmap bitmap = BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
        return bitmap;
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
}