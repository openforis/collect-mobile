package org.openforis.collect.android.gui.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class AndroidFiles {
    /**
     * Workaround for https://code.google.com/p/android/issues/detail?id=38282.
     */
    public static void makeDiscoverable(File file, Context context) {
        if (file.isDirectory() && file.exists())
            makeDirectoryDiscoverable(file, context);
        else
            makeFileDiscoverable(file, context);
    }

    private static void makeFileDiscoverable(File file, Context context) {
        MediaScannerConnection.scanFile(context.getApplicationContext(), new String[]{file.getPath()}, null, null);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(file)));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void makeDirectoryDiscoverable(File dir, Context context) {
        Iterator<File> it = FileUtils.iterateFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        while (it.hasNext()) {
            File file = it.next();
            if (!file.isDirectory())
                makeFileDiscoverable(file, context);
        }
    }

    public static File getFileByUri(Context context, Uri uri) {
        File existingFile = getExistingLocalFileFromUri(context, uri);
        if (existingFile != null)
            return existingFile;
        if (uri.getScheme().equals("content")) {
            String fileName = getUriContentFileName(context, uri);
            File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadDir, fileName);
            if (copyUriContentToFile(context, uri, file)) {
                return file;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String getUriContentFileName(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
                return name;
            } else {
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static boolean copyUriContentToFile(Context context, Uri uri, File file) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null)
                return false;
            IOUtils.copy(inputStream, new FileOutputStream(file));
            inputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Nullable
    public static File getExistingLocalFileFromUri(Context context, Uri uri) {
        try {
            File file = com.ipaulpro.afilechooser.utils.FileUtils.getFile(context, uri);
            return file.exists() ? file : null;
        } catch(Exception e) {
            return null;
        }
    }
}