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

import org.apache.commons.io.IOUtils;
import org.jooq.tools.StringUtils;
import static org.openforis.collect.android.gui.util.AndroidFiles.*;
import org.openforis.collect.utils.Dates;


public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final File LOG_DIRECTORY = new File(Environment.getExternalStorageDirectory(),
            StringUtils.join(new String[]{"OpenForis", "CollectMobile", "log"}, File.separator));

    private Context context;

    public UncaughtExceptionHandler(Context context) {
        this.context = context;
    }

    public void uncaughtException(Thread thread, Throwable exception) {
        writeLogToFile();

        String logs = extractLogInformation(exception);
        reportLogs(logs);

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    private String extractLogInformation(Throwable exception){
        StringBuilder sb = new StringBuilder();

        StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));

        sb.append("************ CAUSE OF ERROR ************" + LINE_SEPARATOR);
        sb.append(stackTrace.toString());

        sb.append( "************ Timestamp ************" + System.currentTimeMillis());

        sb.append(LINE_SEPARATOR+ "************ DEVICE INFORMATION ***********" + LINE_SEPARATOR);
        sb.append("Brand: "+Build.BRAND);
        sb.append(LINE_SEPARATOR);
        sb.append("Device: "+Build.DEVICE);
        sb.append(LINE_SEPARATOR);
        sb.append("Model: "+Build.MODEL);
        sb.append(LINE_SEPARATOR);
        sb.append("Id: "+Build.ID);
        sb.append(LINE_SEPARATOR);
        sb.append("Product: "+Build.PRODUCT);
        sb.append(LINE_SEPARATOR);

        sb.append(LINE_SEPARATOR + "************ BUILD INFO ************" + LINE_SEPARATOR);
        sb.append("SDK: "+Build.VERSION.SDK_INT);
        sb.append(LINE_SEPARATOR);
        sb.append("Release: "+Build.VERSION.RELEASE);
        sb.append(LINE_SEPARATOR);
        sb.append("Incremental: "+Build.VERSION.INCREMENTAL);
        sb.append(LINE_SEPARATOR);

        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo (context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e2) {
        }
        sb.append(LINE_SEPARATOR + "************ APP INFO ************" + LINE_SEPARATOR);
        sb.append("Version name: "+ (info == null ? "-" : info.versionName));
        sb.append(LINE_SEPARATOR);
        sb.append("Version code: "+ (info == null ? "-" : info.versionCode));
        sb.append(LINE_SEPARATOR);

        return sb.toString();
    }

    private File writeLogToFile() {
        //File must be saved to external storage or it wont be readable by the email app.
        LOG_DIRECTORY.mkdirs();

        String logFileName = String.format("error_%s.log", Dates.format(new Date(), "yyyyMMdd_HHmmssSSS"));

        // Extract to file
        File logFile = new File(LOG_DIRECTORY, logFileName);

        FileWriter logFileWriter = null;
        try {
            logFile.createNewFile();

            makeDiscoverable(logFile, context);

            PackageManager manager = context.getPackageManager();
            PackageInfo info = null;
            try {
                info = manager.getPackageInfo (context.getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e2) {
            }
            String model = Build.MODEL;
            if (!model.startsWith(Build.MANUFACTURER))
                model = Build.MANUFACTURER + " " + model;

            // write output stream
            logFileWriter = new FileWriter(logFile);
            logFileWriter.write ("Android version: " +  Build.VERSION.SDK_INT + "\n");
            logFileWriter.write ("Device: " + model + "\n");
            logFileWriter.write ("App version: " + (info == null ? "(null)" : info.versionCode) + "\n");

            writeLogcat(logFileWriter);

            logFileWriter.close();
        } catch (IOException e) {
            IOUtils.closeQuietly(logFileWriter);
            // You might want to write a failure message to the log here.
            return null;
        }
        return logFile;
    }

    private void writeLogcat(FileWriter writer) throws IOException {
        InputStreamReader logCatReader = null;
        try {
            // For Android 4.0 and earlier, you will get all app's log output, so filter it to
            // mostly limit it to your app's output.  In later versions, the filtering isn't needed.
            String cmd = (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) ?
                    "logcat -d -v time MyApp:v dalvikvm:v System.err:v *:s" :
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
        //Logger.LogError("custom error",errorLogs.toString());
        //Open Send log activity
        Intent intent = new Intent();
        intent.setAction(".android.gui.SendLogActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        intent.putExtra("logs", errorLogs.toString());
        context.startActivity(intent);
    }
}