package org.openforis.collect.android.gui.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public abstract class Bitmaps {

     public static boolean saveToFile(Bitmap bitmap, File file, Bitmap.CompressFormat compressFormat) {
        FileOutputStream fo = null;
        try {
            fo = new FileOutputStream(file);
            bitmap.compress(compressFormat, 100, fo);
        } catch (FileNotFoundException e) {
            return false;
        } finally {
            IOUtils.closeQuietly(fo);
        }
        return true;
    }

    public static boolean saveToFile(Bitmap bitmap, File file, Bitmap.CompressFormat compressFormat, Integer maxSizeBytes) {
        // something went wrong storing the bitmap
        if (!saveToFile(bitmap, file, compressFormat)) return false;

        // file is not exceeding maximum allowed
        if (maxSizeBytes == null || file.length() <= maxSizeBytes) return true;

        // reduce file size; find the best scale ratio with a binary search
        boolean fileSaveResult = true,
                exceedingMax = true,
                lowerThanMin = false;

        float tolerance = 0.1f; // 10% of max size (scaled image cannot have a size less than max-size - 10%)
        int minSizeAllowed = Double.valueOf(Math.ceil(maxSizeBytes * (1 - tolerance))).intValue();

        float scaleRatio = 0.5f;

        while (fileSaveResult && (exceedingMax || lowerThanMin)) {
            Bitmap scaledImage = scale(bitmap, scaleRatio);
            fileSaveResult = saveToFile(scaledImage, file, compressFormat);
            exceedingMax = file.length() > maxSizeBytes;
            lowerThanMin = file.length() < minSizeAllowed;
            scaleRatio = (float) (scaleRatio * (exceedingMax ? 0.5: 1.5));
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

    public static void rotateFile(File file) {
        Bitmap source = BitmapFactory.decodeFile(file.getAbsolutePath());
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotated = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
        saveToFile(rotated, file, Bitmap.CompressFormat.JPEG);
    }
}
