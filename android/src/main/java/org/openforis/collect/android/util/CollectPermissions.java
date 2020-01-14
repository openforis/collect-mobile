package org.openforis.collect.android.util;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.Dialogs;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public abstract class CollectPermissions {

    public enum PermissionRequest {
        CAMERA(1, R.string.permission_camera_rationale),
        INTERNET(2),
        STORAGE(3, R.string.permission_storage_rationale),
        READ_EXTERNAL_STORAGE(4),
        WRITE_EXTERNAL_STORAGE(5),
        LOCATION_ACCESS(6, R.string.permission_location_access_rationale),
        RECORD_AUDIO(7),
        CAMERA_BARCODE_SCANNER(8);

        int code;
        Integer messageKey;

        PermissionRequest(int code) {
            this.code = code;
        };

        PermissionRequest(int code, Integer messageKey) {
            this(code);
            this.messageKey = messageKey;
        }

        public int getCode() {
            return code;
        }
    }

    private static void requestPermissions(Activity context, List<String> permissions, int code) {
        ActivityCompat.requestPermissions(context, permissions.toArray(new String[permissions.size()]), code);
    }

    public static boolean checkPermissionsOrRequestThem(final Activity context, final PermissionRequest request, String... permissions) {
        List<String> permissionsNotGranted = new ArrayList<String>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED) {
                permissionsNotGranted.add(permission);
            }
        }
        if (permissionsNotGranted.isEmpty()) {
            return true;
        } else {
            // Permissions not granted: check if a rationale message could be shown
            final List<String> permissionsToAsk = new ArrayList<String>();
            for (String permission: permissionsNotGranted) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                    permissionsToAsk.add(permission);
                }
            }
            if (!permissionsToAsk.isEmpty()) {
                // Request the permission
                if (request.messageKey == null) {
                    requestPermissions(context, permissionsToAsk, request.code);
                } else {
                    // Show rational message to ask for permissions
                    Dialogs.alert(context, R.string.permission_required, request.messageKey, new Runnable() {
                        public void run() {
                            requestPermissions(context, permissionsToAsk, request.code);
                        }
                    });
                }
            }
            return false;
        }
    }

    public static boolean checkCameraPermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PermissionRequest.CAMERA, Manifest.permission.CAMERA);
    }

    public static boolean checkCameraBarcodeScannerPermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PermissionRequest.CAMERA_BARCODE_SCANNER, Manifest.permission.CAMERA);
    }

    public static boolean checkInternetPermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PermissionRequest.INTERNET, Manifest.permission.INTERNET);
    }

    public static boolean checkStoragePermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PermissionRequest.STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean checkReadExternalStoragePermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PermissionRequest.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static boolean checkWriteExternalStoragePermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PermissionRequest.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean checkLocationAccessPermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PermissionRequest.LOCATION_ACCESS,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static boolean checkRecordAudioPermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PermissionRequest.RECORD_AUDIO,
                Manifest.permission.RECORD_AUDIO);
    }

    public static boolean isPermissionGranted(int[] grantResults) {
        return grantResults.length > 0 && grantResults[0] == PERMISSION_GRANTED;
    }
}
