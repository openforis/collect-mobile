package org.openforis.collect.android.util;

import android.Manifest;
import android.app.Activity;
import android.support.v4.app.ActivityCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public abstract class CollectPermissions {

    public static final int PERMISSIONS_REQUEST_CAMERA_CODE = 1;
    public static final int PERMISSIONS_REQUEST_INTERNET_CODE = 2;
    public static final int PERMISSIONS_REQUEST_STORAGE_CODE = 3;
    public static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 4;
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_CODE = 5;
    public static final int PERMISSIONS_REQUEST_ACCESS_LOCATION_CODE = 6;
    public static final int PERMISSIONS_REQUEST_RECORD_AUDIO_CODE = 7;

    public static boolean checkPermissionsOrRequestThem(Activity context, int requestCode, String... permissions) {
        boolean granted = true;
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PERMISSION_GRANTED) {
                granted = false;
                break;
            }
        }
        if (! granted) {
            ActivityCompat.requestPermissions(context, permissions, requestCode);
            return false;
        } else {
            return true;
        }
    }

    public static boolean checkCameraPermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PERMISSIONS_REQUEST_CAMERA_CODE, Manifest.permission.CAMERA);
    }

    public static boolean checkInternetPermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PERMISSIONS_REQUEST_INTERNET_CODE, Manifest.permission.INTERNET);
    }

    public static boolean checkStoragePermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PERMISSIONS_REQUEST_STORAGE_CODE,
                Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean checkReadExternalStoragePermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    public static boolean checkWriteExternalStoragePermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE_CODE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static boolean checkAccessLocationPermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PERMISSIONS_REQUEST_ACCESS_LOCATION_CODE,
                Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static boolean checkRecordAudioPermissionOrRequestIt(Activity context) {
        return checkPermissionsOrRequestThem(context, PERMISSIONS_REQUEST_RECORD_AUDIO_CODE,
                Manifest.permission.RECORD_AUDIO);
    }
}
