package org.openforis.collect.android.gui.util;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.File;
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
        MediaScannerConnection.scanFile(context, new String[]{file.getPath()}, null, null);
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
}