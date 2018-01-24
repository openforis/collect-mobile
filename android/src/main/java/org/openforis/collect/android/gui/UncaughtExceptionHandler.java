package org.openforis.collect.android.gui;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.jooq.tools.StringUtils;
import static org.openforis.collect.android.gui.util.AndroidFiles.*;

import org.openforis.collect.android.gui.util.App;
import org.openforis.collect.utils.Dates;


public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final File LOG_DIRECTORY = new File(Environment.getExternalStorageDirectory(),
            StringUtils.join(new String[]{"OpenForis", "CollectMobile", "log"}, File.separator));

    private Context context;

    UncaughtExceptionHandler(Context context) {
        this.context = context;
    }

    public void uncaughtException(Thread thread, Throwable e) {
        Log.e(CollectMobileApplication.LOG_TAG, e.getMessage(), e);
        try {
            writeLogToFile();
            String logs = extractLogInformation(e);
            reportLogs(logs);
        } catch(Exception ex) {
            //do nothing
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    private String extractLogInformation(Throwable exception){
        StringBuilder sb = new StringBuilder();

        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));

        sb.append("************ CAUSE OF ERROR ************");
        sb.append(LINE_SEPARATOR);
        sb.append(stackTrace.toString());

        sb.append( "************ Timestamp ************");
        sb.append(System.currentTimeMillis());

        sb.append(LINE_SEPARATOR);
        sb.append("************ DEVICE INFORMATION ***********");
        sb.append(LINE_SEPARATOR);
        sb.append("Brand: ");
        sb.append(Build.BRAND);
        sb.append(LINE_SEPARATOR);
        sb.append("Device: ");
        sb.append(Build.DEVICE);
        sb.append(LINE_SEPARATOR);
        sb.append("Model: ");
        sb.append(Build.MODEL);
        sb.append(LINE_SEPARATOR);
        sb.append("Id: ");
        sb.append(Build.ID);
        sb.append(LINE_SEPARATOR);
        sb.append("Product: ");
        sb.append(Build.PRODUCT);
        sb.append(LINE_SEPARATOR);

        sb.append(LINE_SEPARATOR);
        sb.append("************ BUILD INFO ************");
        sb.append(LINE_SEPARATOR);
        sb.append("SDK: ");
        sb.append(Build.VERSION.SDK_INT);
        sb.append(LINE_SEPARATOR);
        sb.append("Release: ");
        sb.append(Build.VERSION.RELEASE);
        sb.append(LINE_SEPARATOR);
        sb.append("Incremental: ");
        sb.append(Build.VERSION.INCREMENTAL);
        sb.append(LINE_SEPARATOR);

        PackageManager manager = context.getPackageManager();
        PackageInfo info;
        try {
            info = manager.getPackageInfo (context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
            info = null;
        }
        sb.append(LINE_SEPARATOR);
        sb.append("************ APP INFO ************");
        sb.append(LINE_SEPARATOR);
        sb.append("Version name: ");
        sb.append(info == null ? "-" : info.versionName);
        sb.append(LINE_SEPARATOR);
        sb.append("Version code: ");
        sb.append(info == null ? "-" : info.versionCode);
        sb.append(LINE_SEPARATOR);

        return sb.toString();
    }

    private void writeLogToFile() {
        //File must be saved to external storage or it wont be readable by the email app.
        if (LOG_DIRECTORY.exists() || LOG_DIRECTORY.mkdirs()) {
            String logFileName = String.format("error_%s.log", Dates.format(new Date(), "yyyyMMdd_HHmmss_SSS"));

            // Extract to file
            File logFile = new File(LOG_DIRECTORY, logFileName);

            FileWriter logFileWriter = null;
            try {
                if (logFile.createNewFile()) {
                    makeDiscoverable(logFile, context);

                    String model = Build.MODEL;
                    if (!model.startsWith(Build.MANUFACTURER))
                        model = Build.MANUFACTURER + " " + model;

                    // write output stream
                    logFileWriter = new FileWriter(logFile);
                    logFileWriter.write("Android version: " + Build.VERSION.SDK_INT + "\n");
                    logFileWriter.write("Device: " + model + "\n");
                    logFileWriter.write("App version: " + App.versionName(context) + "\n");

                    writeLogcat(logFileWriter);
                }
            } catch (IOException e) {
                IOUtils.closeQuietly(logFileWriter);
            }
        }
    }

    private void writeLogcat(FileWriter writer) throws IOException {
        InputStreamReader logCatReader = null;
        try {
            // For Android 4.0 and earlier, you will get all app's log output, so filter it to
            // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
            String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
                    "logcat -d -v time " + CollectMobileApplication.LOG_TAG + ":v dalvikvm:v System.err:v *:s":
                    "logcat -d -v time";
            // get input stream
            Process printLogcatProcess = Runtime.getRuntime().exec(cmd);
            logCatReader = new InputStreamReader(printLogcatProcess.getInputStream());

            IOUtils.copy(logCatReader, writer);
        } finally {
            IOUtils.closeQuietly(logCatReader);
        }
    }

    private void reportLogs(String errorLogs) {
        //Open Send log activity
        Intent intent = new Intent();
        intent.setAction(".android.gui.SendLogActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        intent.putExtra("logs", errorLogs);
        context.startActivity(intent);
    }
}