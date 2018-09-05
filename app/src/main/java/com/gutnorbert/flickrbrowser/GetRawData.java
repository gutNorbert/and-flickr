package com.gutnorbert.flickrbrowser;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

enum DownloadStatus { IDLE,PROCESSING, NOT_INITIALISED,FAILED_OR_EMPTY, OK }

/**
 * Created by Norbi on 2018. 03. 05..
 */

class GetRawData extends AsyncTask<String,Void,String> {
    private static final String TAG = "GetRawData";

    private DownloadStatus mDownloadStatus;
    private final OnDownloadComplete mCallback;

    interface OnDownloadComplete {
        void onDownloadComplete(String data, DownloadStatus status);
    }

    public GetRawData(OnDownloadComplete callback) {
        mDownloadStatus = DownloadStatus.IDLE;
        mCallback = callback;
    }

    void runInSameThread(String s){
        Log.d(TAG, "runInSameThread: starts");

//        onPostExecute(doInBackground(s));
        if (mCallback != null){
//            String result = doInBackground(s);
//            mCallback.onDownloadComplete(result,mDownloadStatus);
            mCallback.onDownloadComplete(doInBackground(s),mDownloadStatus);
        }

        Log.d(TAG, "runInSameThread: ends");
    }

    @Override
    protected void onPostExecute(String s) {
//        Log.d(TAG, "onPostExecute: parameter = " + s);
        if (mCallback != null){
            mCallback.onDownloadComplete(s,mDownloadStatus);
        }
        Log.d(TAG, "onPostExecute: ends");
    }

    @Override
    protected String doInBackground(String... strings) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        if (strings == null){
            mDownloadStatus = DownloadStatus.NOT_INITIALISED;
        }

        try {
            mDownloadStatus = DownloadStatus.PROCESSING;
            URL url = new URL(strings[0]);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int response = connection.getResponseCode();
            Log.d(TAG, "doInBackground: The response code was " + response);

            StringBuilder result = new StringBuilder();

            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

//            String line;
//            while (null != (line = reader.readLine())){
            for(String line = reader.readLine(); line != null; line = reader.readLine()){
                result.append(line).append("\n");
            }

            mDownloadStatus = DownloadStatus.OK;
            return result.toString();

        } catch(MalformedURLException e){ //log e will be written to log files
            Log.e(TAG, "instance initializer: Invalid URL" + e.getMessage());
        } catch(IOException e) {
            Log.e(TAG, "instance initializer: IO Exception reading data" + e.getMessage());
        } catch(SecurityException e){
            Log.e(TAG, "instance initializer: Security Exception. Needs permission?" + e.getMessage());
        } finally { //exception thrown, executes before return
            if (connection != null){
                connection.disconnect();
            }
            if (reader != null){
                try{
                    reader.close();
                } catch (IOException e){
                    Log.e(TAG, "doInBackground: Error closing stream" + e.getMessage());
                }
            }
        }

        mDownloadStatus = DownloadStatus.FAILED_OR_EMPTY;
        return null;
    }

}
