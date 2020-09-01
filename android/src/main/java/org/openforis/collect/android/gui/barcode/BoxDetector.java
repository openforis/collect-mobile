package org.openforis.collect.android.gui.barcode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.ByteArrayOutputStream;

public class BoxDetector extends Detector<Barcode> {
    private Detector mDelegate;
    private Rect croppingRect;

    public BoxDetector(Detector delegate) {
        mDelegate = delegate;
    }

    public void setCroppingRect(Rect rect) {
        this.croppingRect = rect;
    }

    public SparseArray detect(Frame frame) {
        /*
        Frame croppedFrame = this.croppingRect == null ? frame
                : new Frame.Builder()
                        .setBitmap(crop(frame))
                        .setRotation(frame.getMetadata().getRotation())
                        .build();
        Bitmap originalBitmap = frame.getBitmap();
        */
        return mDelegate.detect(frame);
    }

    private Bitmap crop(Frame frame) {
        int width = frame.getMetadata().getWidth();
        int height = frame.getMetadata().getHeight();

        Rect rect = croppingRect.right > width || croppingRect.bottom > height ?
                rotateRectClockwise(croppingRect)
                : croppingRect;

        if (rect.width() <= 0 || rect.height() <= 0) {
            return frame.getBitmap();
        }

        try {
            YuvImage yuvImage = new YuvImage(frame.getGrayscaleImageData().array(), ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            yuvImage.compressToJpeg(rect, 100, byteArrayOutputStream);
            byte[] jpegArray = byteArrayOutputStream.toByteArray();
            return BitmapFactory.decodeByteArray(jpegArray, 0, jpegArray.length);
        } catch(Exception e) {
            return frame.getBitmap();
        }
    }

    @NonNull
    private Rect rotateRectClockwise(Rect rect) {
        return new Rect(rect.top, rect.left, rect.bottom, rect.right);
    }

    public boolean isOperational() {
        return mDelegate.isOperational();
    }

    public boolean setFocus(int id) {
        return mDelegate.setFocus(id);
    }
}