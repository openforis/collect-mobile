package org.openforis.collect.android.gui.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public abstract class Bitmaps {

    public static boolean saveToFile(Bitmap bitmap, File file) {
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fo);
        } catch (FileNotFoundException e) {
            return false;
        } finally {
            IOUtils.closeQuietly(fo);
        }
        return true;
    }

    public static boolean saveToFile(Bitmap bitmap, File file, Integer maxSizeBytes) {
        if (!saveToFile(bitmap, file)) return false;

        if (maxSizeBytes == null || file.length() <= maxSizeBytes) return true;

        boolean fileSaveResult = true,
                exceedingMax = true,
                exceedingMin = false;
        float lastScaleRatio = 1;

        float TOLERANCE_MIN = (float) 0.1; // 10% of max size (scaled image cannot have a size less than max-size - 10%)
        
        while (fileSaveResult && (exceedingMax || exceedingMin)) {
            float ratio = exceedingMax ? lastScaleRatio / 2 : (float) (lastScaleRatio * 1.5);
            Bitmap scaledImage = scale(bitmap, ratio);
            fileSaveResult = saveToFile(scaledImage, file);
            lastScaleRatio = ratio;
            exceedingMax = file.length() > maxSizeBytes;
            exceedingMin = file.length() < maxSizeBytes * (1 - TOLERANCE_MIN);
        }
        return fileSaveResult;
    }

    public static Bitmap scaleToFit(String filePath, int maxWidth, int maxHeight) {
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

    public static Bitmap scaleToFit(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float ratio = Math.min(
                (float) maxWidth / width,
                (float) maxHeight / height);
        return scale(bitmap, ratio);
    }

    public static Bitmap scale(Bitmap bitmap, float ratio) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int scaledWith = Math.round(ratio * width);
        int scaledHeight = Math.round(ratio * height);
        return Bitmap.createScaledBitmap(bitmap, scaledWith, scaledHeight, false);
    }
}
