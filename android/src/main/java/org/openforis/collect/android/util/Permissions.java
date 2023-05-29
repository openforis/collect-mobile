package org.openforis.collect.android.util;

import android.Manifest;
import android.app.Activity;
import androidx.core.app.ActivityCompat;
import android.widget.Toast;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.AndroidVersion;
import org.openforis.collect.android.gui.util.Dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public abstract class Permissions {

    public enum Request {
        CAMERA(1, R.string.permission_camera),
        INTERNET(2, R.string.permission_internet, R.string.permission_internet_rationale),
        STORAGE(3, R.string.permission_storage, R.string.permission_storage_rationale),
        READ_EXTERNAL_STORAGE(4, R.string.permission_external_storage),
        WRITE_EXTERNAL_STORAGE(5, R.string.permission_external_storage),
        LOCATION_ACCESS(6, R.string.permission_location),
        RECORD_AUDIO(7, R.string.permission_record_audio),
        CAMERA_BARCODE_SCANNER(8, R.string.permission_camera_barcode_scanner);

        int code;
        int permissionMessageKey;
        Integer rationaleMessageKey;

        Request(int code) {
            this.code = code;
        }

        Request(int code, int permissionMessageKey) {
            this(code, permissionMessageKey, null);
        }

        Request(int code, int permissionMessageKey, Integer rationaleMessageKey) {
            this(code);
            this.permissionMessageKey = permissionMessageKey;
            this.rationaleMessageKey = rationaleMessageKey;
        }

        public int getCode() {
            return code;
        }
    }

    private enum Status {
        GRANTED,
        NOT_GRANTED,
        SHOULD_SHOW_RATIONALE
    }

    private static final String[] readStoragePermissions;
    static {
        List<String> readStoragePermissionsList = new ArrayList<String>();
        if (AndroidVersion.greaterEqualThan33()) {
            readStoragePermissionsList.addAll(Arrays.asList(
                    Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO));
        } else {
            readStoragePermissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        readStoragePermissions = readStoragePermissionsList.toArray(new String[readStoragePermissionsList.size()]);
    }

    private static final String[] storagePermissions;
    static {
        List<String> storagePermissionsList = new ArrayList<String>(Arrays.asList(readStoragePermissions));
        if (!AndroidVersion.greaterEqualThan33()) {
            storagePermissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        storagePermissions = storagePermissionsList.toArray(new String[storagePermissionsList.size()]);
    }

    private static void requestPermissions(Activity context, List<String> permissions, int code) {
        ActivityCompat.requestPermissions(context, permissions.toArray(new String[permissions.size()]), code);
    }

    private static boolean checkPermissionsOrRequestThem(final Activity context, final Request request, String... permissions) {
        List<String> permissionsNotGranted = new ArrayList<String>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED) {
                permissionsNotGranted.add(permission);
            }
        }
        if (permissionsNotGranted.isEmpty()) {
           return true;
        }
        // Permissions not granted: check if a rationale message could be shown
        final List<String> permissionsToAsk = new ArrayList<String>();
        for (String permission: permissionsNotGranted) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                permissionsToAsk.add(permission);
            }
        }
        if (permissionsToAsk.isEmpty()) {
            requestPermissions(context, permissionsNotGranted, request.code);
            Toast.makeText(context, getDeniedMessage(context, request), Toast.LENGTH_LONG).show();
        } else {
            // Show rational message to ask for permissions
            String rationaleMessage = getRationaleMessage(context, request);
            Dialogs.alert(context, context.getString(R.string.permission_required), rationaleMessage, new Runnable() {
                public void run() {
                    requestPermissions(context, permissionsToAsk, request.code);
                }
            });
        }
        return false;
    }

    public static boolean checkCameraPermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, Request.CAMERA, Manifest.permission.CAMERA);
    }

    public static boolean checkCameraBarcodeScannerPermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, Request.CAMERA_BARCODE_SCANNER, Manifest.permission.CAMERA);
    }

    public static boolean checkInternetPermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, Request.INTERNET, Manifest.permission.INTERNET);
    }

    public static boolean checkStoragePermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, Request.STORAGE, storagePermissions);
    }

    public static boolean checkReadExternalStoragePermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, Request.READ_EXTERNAL_STORAGE, readStoragePermissions);
    }

    public static boolean checkLocationAccessPermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, Request.LOCATION_ACCESS,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static boolean checkRecordAudioPermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, Request.RECORD_AUDIO,
                Manifest.permission.RECORD_AUDIO);
    }

    public static boolean isPermissionGranted(int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED;
    }

    public static String getDeniedMessage(Activity context, Request request) {
        return context.getString(R.string.permission_request_prefix) + " " +
                context.getString(request.permissionMessageKey) + " " +
                context.getString(R.string.permission_denied_suffix);
    }

    private static String getRationaleMessage(Activity context, Request request) {
        return context.getString(R.string.permission_request_prefix) + " " +
                context.getString(request.permissionMessageKey) + " " +
                (request.rationaleMessageKey == null ? "" : context.getString(request.rationaleMessageKey) +  " ") +
                context.getString(R.string.permission_request_rationale_suffix);
    }
}
