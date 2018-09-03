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
    private int left, top, right, bottom;

    public BoxDetector(Detector delegate, int left, int top, int right, int bottom) {
        mDelegate = delegate;
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public SparseArray detect(Frame frame) {
        int width = frame.getMetadata().getWidth();
        int height = frame.getMetadata().getHeight();

        Bitmap bitmap = toBitmap(frame, width, height);

        Frame croppedFrame =
                new Frame.Builder()
                        .setBitmap(bitmap)
                        .setRotation(frame.getMetadata().getRotation())
                        .build();

        return mDelegate.detect(croppedFrame);
    }

    private Bitmap toBitmap(Frame frame, int width, int height) {
        YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(left, top, right, bottom), 100, byteArrayOutputStream);
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