package org.openforis.collect.android.gui;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;
import org.openforis.collect.R;
import org.openforis.collect.android.collectadapter.SurveyExporter;
import org.openforis.collect.android.gui.settings.SettingsActivity;
import org.openforis.collect.android.gui.util.AppDirs;
import org.openforis.collect.android.util.HttpConnectionHelper;
import org.openforis.collect.android.util.MultipartUtility;
import org.openforis.collect.android.util.ProgressHandler;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static org.openforis.collect.android.gui.CollectMobileApplication.LOG_TAG;

public class SubmitDataToCollectActivity extends BaseActivity {

    private static final String DATA_RESTORE_ENDPOINT = "/api/surveys/restore/data";
    private static final String DATA_RESTORE_JOB_ENDPOINT = "/api/surveys/data/restorejobs/%s/status.json";
    private static final long RESTORE_DATA_JOB_MONITOR_PERIOD = 3000L;

    private enum ViewState {
        EXPORTING_DATA, UPLOADING, RESTORING, ERROR, COMPLETE, ABORTED
    }

    private File exportedFile;
    private String remoteAddress;
    private String remoteUsername;
    private String remotePassword;
    private String surveyName;

    private TextView currentTaskTitleText;
    private LinearLayout taskRunningContainer;
    private ProgressBar indeterminateProgressBar;
    private ProgressBar progressBar;
    private LinearLayout errorContainer;
    private TextView errorMessageText;
    private TextView dataSubmitCancelledMessageText;
    private TextView dataSubmitCompletedMessageText;
    private Button cancelButton;
    private Timer jobMonitorTimer;
    private AsyncTask uploadTask;
    private ViewState viewState = ViewState.UPLOADING;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.submit_data_to_collect);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        remoteAddress = preferences.getString(SettingsActivity.REMOTE_COLLECT_ADDRESS, "");
        remoteUsername = preferences.getString(SettingsActivity.REMOTE_COLLECT_USERNAME, "");
        remotePassword = preferences.getString(SettingsActivity.REMOTE_COLLECT_PASSWORD, "");

        errorContainer = (LinearLayout) findViewById(R.id.errorContainer);
        errorMessageText = (TextView) findViewById(R.id.errorMessageText);

        currentTaskTitleText = (TextView) findViewById(R.id.submitToCollectCurrentTaskText);
        taskRunningContainer = (LinearLayout) findViewById(R.id.taskRunningContainer);
        indeterminateProgressBar = (ProgressBar) findViewById(R.id.submitToCollectIndeterminateProgressBar);
        progressBar = (ProgressBar) findViewById(R.id.submitToCollectProgressBar);
        progressBar.setProgress(0);

        cancelButton = (Button) findViewById(R.id.cancelRemoteDataRestoreBtn);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                handleCancelClick();
            }
        });

        dataSubmitCancelledMessageText = (TextView) findViewById(R.id.submitToCollectCancelledMessageText);
        dataSubmitCompletedMessageText = (TextView) findViewById(R.id.submitToCollectCompletedMessageText);

        surveyName = ServiceLocator.surveyService().getSelectedSurvey().getName();
        updateViewState(ViewState.EXPORTING_DATA);
        new ExportDataTask().execute();
    }

    private void handleCancelClick() {
        this.cancelButton.setEnabled(false); //avoid double click
        switch(SubmitDataToCollectActivity.this.viewState) {
            case UPLOADING:
                uploadTask.cancel(true);
                break;

        }
    }

    private void handleError(final String error) {
        Log.e(LOG_TAG, "Send data to Collect error: " + error);

        updateViewState(ViewState.ERROR, new Runnable() {
            public void run() {
                errorMessageText.setText(error);
            }
        });

        if (jobMonitorTimer != null) {
            jobMonitorTimer.cancel();
        }
    }

    private void updateViewState(final ViewState state) {
        updateViewState(state, null);
    }

    private void updateViewState(final ViewState state, final Runnable callback) {
        Log.d(LOG_TAG, "Update view state : " + state);
        this.viewState = state;
        runOnUiThread(new Runnable() {
            public void run() {
                indeterminateProgressBar.setVisibility(INVISIBLE);
                progressBar.setVisibility(INVISIBLE);
                errorContainer.setVisibility(INVISIBLE);
                taskRunningContainer.setVisibility(INVISIBLE);
                cancelButton.setVisibility(INVISIBLE);
                Integer taskTitleId = null;
                switch (viewState) {
                    case EXPORTING_DATA:
                        taskTitleId = R.string.submit_to_collect_exporting_data_title;
                        taskRunningContainer.setVisibility(VISIBLE);
                        indeterminateProgressBar.setVisibility(VISIBLE);
                        break;
                    case UPLOADING:
                        taskTitleId = R.string.submit_to_collect_uploading_file_title;
                        progressBar.setVisibility(VISIBLE);
                        taskRunningContainer.setVisibility(VISIBLE);
                        cancelButton.setVisibility(VISIBLE);
                        break;
                    case RESTORING:
                        taskTitleId = R.string.submit_to_collect_restoring_data_title;
                        progressBar.setVisibility(VISIBLE);
                        taskRunningContainer.setVisibility(VISIBLE);
                        cancelButton.setVisibility(VISIBLE);
                        break;
                    case ERROR:
                        errorContainer.setVisibility(VISIBLE);
                        break;
                    case ABORTED:
                        dataSubmitCancelledMessageText.setVisibility(VISIBLE);
                        break;
                    case COMPLETE:
                        dataSubmitCompletedMessageText.setVisibility(VISIBLE);
                        break;
                }
                currentTaskTitleText.setText(taskTitleId != null ? getResources().getString(taskTitleId): "");

                if (callback != null) {
                    callback.run();
                }
            }
        });
    }

    private void startExportedFileUpload() {
        uploadTask = new UploadBackupFileTask().execute();
    }

    private void startJobMonitor(String url) {
        updateViewState(ViewState.RESTORING, null);
        jobMonitorTimer = new Timer();
        final JobMonitorTask task = new JobMonitorTask(url, new JobStatusResponseProcessor() {
            public void process(final JobStatusResponse response) {
                String status = response.getJobStatus();
                if ("RUNNING".equals(status)) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            progressBar.setProgress(response.getJobProgress());
                        }
                    });
                } else {
                    jobMonitorTimer.cancel();
                    if ("COMPLETED".equals(status)) {
                        updateViewState(ViewState.COMPLETE);
                    } else if ("ABORTED".equals(status)) {
                        updateViewState(ViewState.ABORTED);
                    } else if ("FAILED".equals(status)) {
                        handleError(response.getJobErrorMessage());
                    }
                }
            }
        });
        jobMonitorTimer.scheduleAtFixedRate(task, 0, RESTORE_DATA_JOB_MONITOR_PERIOD);
    }

    private class ExportDataTask extends AsyncTask<Void, Void, File> {

        @Override
        protected File doInBackground(Void... voids) {
            SubmitDataToCollectActivity context = SubmitDataToCollectActivity.this;
            try {
                return ServiceLocator.surveyService().exportSurvey(AppDirs.surveysDir(context),false, null);
            } catch (SurveyExporter.AllRecordKeysNotSpecified e) {
                handleError(AllRecordKeysNotSpecifiedDialog.generateMessage(context));
            } catch (Exception e) {
                handleError(e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(File exportedFile) {
            super.onPostExecute(exportedFile);
            if (exportedFile == null) {
                updateViewState(ViewState.ERROR);
            } else {
                SubmitDataToCollectActivity.this.exportedFile = exportedFile;
                startExportedFileUpload();
            }
        }
    }

    private class UploadBackupFileTask extends AsyncTask<String, Integer, String> {

        private MultipartUtility multipart;

        @Override
        protected String doInBackground(String... args) {
            String uploadUrl = remoteAddress + DATA_RESTORE_ENDPOINT;
            Log.d(LOG_TAG, "Uploading data file to: " + uploadUrl);

            try {
                multipart = new MultipartUtility(uploadUrl, remoteUsername, remotePassword);
                multipart.addFormField("surveyName", surveyName);
                multipart.addFilePart("file", exportedFile, new ProgressHandler() {
                    public void onProgress(int progressPercent) {
                        onProgressUpdate(progressPercent);
                    }
                });
                String response = multipart.finish();
                return response;
            } catch(Exception e) {
                handleError(e.getMessage());
                return null;
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            multipart.cancel();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            final int progressPercent = values[0];
            Log.d(LOG_TAG, String.valueOf(progressPercent));
            runOnUiThread(new Runnable() {
                public void run() {
                    progressBar.setProgress(progressPercent);
                }
            });
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            if (response != null) {
                Log.d(LOG_TAG, response);
                try {
                    JSONObject jsonResp = new JSONObject(response);
                    String status = jsonResp.getString("status");
                    if ("ERROR".equals(status)) {
                        handleError(jsonResp.getString("errorMessage"));
                    } else {
                        String jobId = jsonResp.getString("jobId");
                        String jobMonitorUrl = remoteAddress + String.format(DATA_RESTORE_JOB_ENDPOINT, jobId);
                        startJobMonitor(jobMonitorUrl);
                    }
                } catch (JSONException e) {
                    handleError(e.getMessage());
                }
            }
        }
    }

    private class JobMonitorTask extends TimerTask {

        private final String jobMonitorUrl;
        private JobStatusResponseProcessor jobProcessor;

        public JobMonitorTask(String jobMonitorUrl, JobStatusResponseProcessor jobProcessor) {
            super();
            this.jobMonitorUrl = jobMonitorUrl;
            this.jobProcessor = jobProcessor;
        }

        @Override
        public void run() {
            Log.d(LOG_TAG, "Job monitored url: " + jobMonitorUrl);
            JsonObject jsonObject = null;
            try {
                jsonObject = new HttpConnectionHelper(jobMonitorUrl, remoteUsername, remotePassword).getJson();
                Gson gson = new Gson();
                JobStatusResponse jobStatus = gson.fromJson(jsonObject, JobStatusResponse.class);
                jobProcessor.process(jobStatus);
            } catch (IOException e) {
                handleError(e.getMessage());
            }
        }
    }

    private interface JobStatusResponseProcessor {

        void process(JobStatusResponse job);

    }

    private class JobStatusResponse {

        private String jobId;
        private String jobStatus;
        private String jobErrorMessage;
        private int jobProgress;

        public String getJobId() {
            return jobId;
        }

        public void setJobId(String jobId) {
            this.jobId = jobId;
        }

        public String getJobStatus() {
            return jobStatus;
        }

        public void setJobStatus(String jobStatus) {
            this.jobStatus = jobStatus;
        }

        public String getJobErrorMessage() {
            return jobErrorMessage;
        }

        public void setJobErrorMessage(String jobErrorMessage) {
            this.jobErrorMessage = jobErrorMessage;
        }

        public int getJobProgress() {
            return jobProgress;
        }

        public void setJobProgress(int jobProgress) {
            this.jobProgress = jobProgress;
        }
    }
}
