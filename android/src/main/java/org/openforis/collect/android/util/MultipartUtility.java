package org.openforis.collect.android.util;

import android.util.Base64;

import static java.lang.String.format;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MultipartUtility {

    private static final String DOUBLE_HYPHEN = "--";

    private final String boundary;
    private static final String LINE_FEED = "\r\n";
    private HttpURLConnection httpConn;
    private String charset;
    private OutputStream outputStream;
    private PrintWriter writer;
    private boolean cancelled = false;
    private boolean finished = false;

    public MultipartUtility(String requestURL, String username, String password) throws IOException {
        this(requestURL, "UTF-8", username, password);
    }

    /**
     * This constructor initializes a new HTTP POST request with content type
     * is set to multipart/form-data
     *
     * @param requestURL
     * @param charset
     * @throws IOException
     */
    public MultipartUtility(String requestURL, String charset, String username, String password)
            throws IOException {
        this.charset = charset;

        // creates a unique boundary based on time stamp
        boundary = "-------FormBoundary" + System.currentTimeMillis();

        URL url = new URL(requestURL);
        httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setUseCaches(false);
        httpConn.setDoOutput(true); // indicates POST method
        httpConn.setDoInput(true);
        httpConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);
        //httpConn.setRequestProperty("User-Agent", "CodeJava Agent");

        if (username != null && password != null) {
            addBasicAuthorization(username, password);
        }

        outputStream = httpConn.getOutputStream();
        writer = new PrintWriter(new OutputStreamWriter(outputStream, charset),
                true);
    }

    private void addBasicAuthorization(String username, String password) {
        String encodedUsernamePassword = Base64.encodeToString((username + ":" + password).getBytes(), Base64.DEFAULT);
        httpConn.setRequestProperty("Authorization", "Basic " + encodedUsernamePassword);
    }

    /**
     * Adds a form field to the request
     *
     * @param name  field name
     * @param value field value
     */
    public void addFormField(String name, String value) {
        writer.append(DOUBLE_HYPHEN + boundary).append(LINE_FEED);
        writer.append(format("Content-Disposition: form-data; name=\"%s\"", name))
                .append(LINE_FEED);
        writer.append(format("Content-Type: text/plain; charset=%s", charset)).append(
                LINE_FEED);
        writer.append(LINE_FEED);
        writer.append(value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Adds a upload file section to the request
     *
     * @param fieldName  name attribute in <input type="file" name="..." />
     * @param uploadFile a File to be uploaded
     * @throws IOException
     */
    public void addFilePart(final String fieldName, final File uploadFile, final ProgressHandler progressHandler)
            throws IOException {
        String fileName = uploadFile.getName();
        writer.append(DOUBLE_HYPHEN + boundary).append(LINE_FEED);
        writer.append(format("Content-Disposition: form-data; name=\"%s\"; filename=\"%s\"", fieldName, fileName)).append(LINE_FEED);
        writer.append("Content-Type: "+ URLConnection.guessContentTypeFromName(fileName)).append(LINE_FEED);
        writer.append("Content-Transfer-Encoding: binary").append(LINE_FEED);
        writer.append(LINE_FEED);
        writer.flush();

        FileInputStream inputStream = new FileInputStream(uploadFile);
        byte[] buffer = new byte[4096];
        int bytesRead = -1;
        int totalBytesRead = 0;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            if (cancelled) {
                return;
            }
            outputStream.write(buffer, 0, bytesRead);
            totalBytesRead += bytesRead;
            progressHandler.onProgress((int) ((totalBytesRead * 100) / uploadFile.length()));
        }
        outputStream.flush();
        inputStream.close();

        writer.append(LINE_FEED);
        writer.flush();
    }

    public void cancel() {
        this.cancelled = true;
    }

    /**
     * Adds a header field to the request.
     *
     * @param name  - name of the header field
     * @param value - value of the header field
     */
    public void addHeaderField(String name, String value) {
        writer.append(name + ": " + value).append(LINE_FEED);
        writer.flush();
    }

    /**
     * Completes the request and receives response from the server.
     *
     * @return a list of Strings as response in case the server returned
     * status OK, otherwise an exception is thrown.
     * @throws IOException
     */
    public String finish() throws IOException {
        StringBuffer response = new StringBuffer();

        writer.append(LINE_FEED).flush();
        writer.append(DOUBLE_HYPHEN + boundary + DOUBLE_HYPHEN).append(LINE_FEED);
        writer.close();

        // checks server's status code first
        int status = httpConn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpConn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            httpConn.disconnect();
        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
        this.finished = true;
        return response.toString();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public boolean isFinished() {
        return finished;
    }
}
