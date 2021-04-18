package org.openforis.collect.android.gui.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.StatFs;
import android.provider.MediaStore;

import androidx.core.content.FileProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.openforis.collect.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public abstract class AndroidFiles {

    private static final String FILE_PROVIDER_AUTHORITY = "org.openforis.collect.fileprovider";
    public static final String COLTRANE_FILE_PICKER_URI_EXTRA_NAME = "com.mobilejazz.coltrane.ui.browser.result.document.id";

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

    private static void makeDirectoryDiscoverable(File dir, Context context) {
        Iterator<File> it = FileUtils.iterateFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        while (it.hasNext()) {
            File file = it.next();
            if (!file.isDirectory())
                makeFileDiscoverable(file, context);
        }
    }

    public static Uri getUriForFile(Context context, File file) {
        return FileProvider.getUriForFile(context, FILE_PROVIDER_AUTHORITY, file);
    }

    public static String getUriContentFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            String[] pathParts = uri.getPath().split("/");
            result = pathParts[pathParts.length - 1];
        }
        return result;
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

    public static File copyUriContentToCache(Context context, Uri uri) {
        try {
            ParcelFileDescriptor fileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r");
            String fileName = getUriContentFileName(context, uri);
            InputStream is = new FileInputStream(fileDescriptor.getFileDescriptor());
            File fileCache = new File(context.getCacheDir(), fileName);
            IOUtils.copy(is, new FileOutputStream(fileCache));
            return fileCache;
        } catch (Exception e) {
            return null;
        }
    }

    public static void showFileChooseActivity(Activity context, int requestCode) {
        Intent target = new Intent(Intent.ACTION_GET_CONTENT);
        target.setType("*/*");
        target.addCategory(Intent.CATEGORY_OPENABLE);
        Intent intent = Intent.createChooser(
                target, context.getString(R.string.select_survey_to_import));
        context.startActivityForResult(intent, requestCode);
    }

    public static Uri getUriFromGetContentIntent(Intent data) {
        if (data.getData() != null) {
            return data.getData();
        }
        // Coltrane file picker activity result
        String uri = data.getStringExtra(COLTRANE_FILE_PICKER_URI_EXTRA_NAME);
        return Uri.parse(String.format("file://%s", uri));
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