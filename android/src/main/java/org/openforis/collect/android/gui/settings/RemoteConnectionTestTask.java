package org.openforis.collect.android.gui.settings;

import android.app.Activity;

import com.google.gson.JsonObject;

import org.openforis.collect.Collect;
import org.openforis.collect.R;
import org.openforis.collect.android.gui.util.Dialogs;
import org.openforis.collect.android.gui.util.SlowAsyncTask;
import org.openforis.collect.android.util.HttpConnectionHelper;
import org.openforis.commons.versioning.Version;

import java.io.FileNotFoundException;

class RemoteConnectionTestTask extends SlowAsyncTask<Void, Void, JsonObject> {

    private final String address;
    private final String username;
    private final String password;

    RemoteConnectionTestTask(Activity context, String address, String username, String password) {
        super(context);
        this.address = address;
        this.username = username;
        this.password = password;
    }

    protected JsonObject runTask() throws Exception {
        HttpConnectionHelper connectionHelper = new HttpConnectionHelper(address, username, password);
        return connectionHelper.getJson();
    }

    @Override
    protected void onPostExecute(JsonObject info) {
        super.onPostExecute(info);
        if (info != null) {
            String remoteCollectVersionStr = info.get("version").getAsString();
            Version remoteCollectVersion = new Version(remoteCollectVersionStr);

            if (Collect.VERSION.compareTo(remoteCollectVersion, Version.Significance.MINOR) > 0) {
                String message = context.getString(R.string.settings_remote_sync_test_failed_message_newer_version,
                        remoteCollectVersion.toString(), Collect.VERSION.toString());
                Dialogs.alert(context, context.getString(R.string.settings_remote_sync_test_failed_title), message);
            } else {
                Dialogs.alert(context, context.getString(R.string.settings_remote_sync_test_successful_title),
                        context.getString(R.string.settings_remote_sync_test_successful_message));
            }
        }
    }

    @Override
    protected void handleException(Exception e) {
        super.handleException(e);
        String message;
        if (e instanceof FileNotFoundException) {
            message = context.getString(R.string.settings_remote_sync_test_failed_message_wrong_address);
        } else {
            message = e.getMessage();
        }
        Dialogs.alert(context, context.getString(R.string.settings_remote_sync_test_failed_title), message);
    }
}
