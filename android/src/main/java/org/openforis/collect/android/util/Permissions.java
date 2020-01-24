package org.openforis.collect.android.util;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.Dialogs;

import java.util.ArrayList;
import java.util.List;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public abstract class Permissions {

    public enum Request {
        CAMERA(1, R.string.permission_camera_rationale, R.string.permission_camera_denied),
        INTERNET(2, R.string.permission_internet_rationale, R.string.permission_internet_denied),
        STORAGE(3, R.string.permission_storage_rationale, R.string.permission_storage_denied),
        READ_EXTERNAL_STORAGE(4, R.string.permission_read_external_storage_rationale, R.string.permission_read_external_storage_denied),
        WRITE_EXTERNAL_STORAGE(5, R.string.permission_write_external_storage_rationale, R.string.permission_write_external_storage_denied),
        LOCATION_ACCESS(6, R.string.permission_location_access_rationale, R.string.permission_location_access_denied),
        RECORD_AUDIO(7, R.string.permission_record_audio_rationale, R.string.permission_record_audio_denied),
        CAMERA_BARCODE_SCANNER(8, R.string.permission_camera_barcode_scanner_rationale, R.string.permission_camera_barcode_scanner_denied);

        int code;
        Integer rationaleMessageKey;
        Integer deniedMessageKey;

        Request(int code) {
            this.code = code;
        };

        Request(int code, Integer rationaleMessageKey, Integer deniedMessageKey) {
            this(code);
            this.rationaleMessageKey = rationaleMessageKey;
            this.deniedMessageKey = deniedMessageKey;
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

    private static void requestPermissions(Activity context, List<String> permissions, int code) {
        ActivityCompat.requestPermissions(context, permissions.toArray(new String[permissions.size()]), code);
    }

    private static boolean checkPermissionsOrRequestThem(final Activity context, final Request request, String... permissions) {
        Status status;
        List<String> permissionsNotGranted = new ArrayList<String>();
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED) {
                permissionsNotGranted.add(permission);
            }
        }
        if (permissionsNotGranted.isEmpty()) {
            status = Status.GRANTED;
        } else {
            // Permissions not granted: check if a rationale message could be shown
            final List<String> permissionsToAsk = new ArrayList<String>();
            for (String permission: permissionsNotGranted) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                    permissionsToAsk.add(permission);
                }
            }
            if (permissionsToAsk.isEmpty()) {
                status = Status.NOT_GRANTED;
            } else {
                // Show rational message to ask for permissions
                Dialogs.alert(context, R.string.permission_required, request.rationaleMessageKey, new Runnable() {
                    public void run() {
                        requestPermissions(context, permissionsToAsk, request.code);
                    }
                });
                status = Status.SHOULD_SHOW_RATIONALE;
            }
        }
        switch (status) {
            case GRANTED:
                return true;
            case NOT_GRANTED:
                Dialogs.alert(context, R.string.permission_required, request.deniedMessageKey);
                return false;
            default:
                return false;
        }
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
        return checkPermissionsOrRequestThem(context, Request.STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean checkReadExternalStoragePermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, Request.READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static boolean checkWriteExternalStoragePermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, Request.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
}
