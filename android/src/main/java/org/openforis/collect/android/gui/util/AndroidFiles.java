package org.openforis.collect.android.gui.util;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public abstract class AndroidFiles {

    private static final String FILE_PROVIDER_AUTHORITY = "org.openforis.collect.fileprovider";

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
            if (file.exists() && file.length() > 0 || copyUriContentToFile(context, uri, file)) {
                return file;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file);
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
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return false;
            } else {
                IOUtils.copy(inputStream, new FileOutputStream(file));
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    @Nullable
    public static File getExistingLocalFileFromUri(Context context, Uri uri) {
        try {
            File file = com.ipaulpro.afilechooser.utils.FileUtils.getFile(context, uri);
            return file.exists() ? file : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static long availableSize(File path) {
        try {
            File existingPath = firstExistingAncestorOrSelf(path);
            StatFs stat = new StatFs(existingPath.getPath());
            long blockSize = AndroidVersion.greaterThan18() ? stat.getBlockSizeLong() : stat.getBlockSize();
            long availableBlocks = AndroidVersion.greaterThan18() ? stat.getAvailableBlocksLong() : stat.getAvailableBlocks();
            return availableBlocks * blockSize;
        } catch (Exception ignore) {
            return Long.MAX_VALUE;
        }
    }

    public static boolean enoughSpaceToCopy(File fromPath, File toPath) {
        return FileUtils.sizeOfDirectory(fromPath) < availableSize(toPath);
    }

    private static File firstExistingAncestorOrSelf(File file) {
        File current = file;
        while (!current.exists()) {
            current = current.getParentFile();
        }
        return current;
    }
}