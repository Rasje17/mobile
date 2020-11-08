package com.dorvis.activityrecognition;

import android.app.IntentService;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.RequiresApi;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import com.opencsv.CSVWriter;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;


@RequiresApi(api = Build.VERSION_CODES.KITKAT)
public class DetectedActivitiesIntentService  extends IntentService {

    String baseDir = android.os.Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    String fileName = "AnalysisData.csv";
    String filePath = baseDir + File.separator + fileName;
    File f = new File(filePath);
    CSVWriter writer;

    long last_log = System.currentTimeMillis()- 5000;

    protected static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();

    public DetectedActivitiesIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        // Get the list of the probable activities associated with the current state of the
        // device. Each activity is associated with a confidence level, which is an int between
        // 0 and 100.
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();

        for (DetectedActivity activity : detectedActivities) {
            Log.i(TAG, "Detected activity: " + activity.getType() + ", " + activity.getConfidence());
            broadcastActivity(activity);
        }
        writeToFile(result.getMostProbableActivity());
    }

    private void broadcastActivity(DetectedActivity activity) {
        Intent intent = new Intent(Constants.BROADCAST_DETECTED_ACTIVITY);
        intent.putExtra("type", activity.getType());
        intent.putExtra("confidence", activity.getConfidence());
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void writeToFile(DetectedActivity activity){


        String[] actCon = new String[]{"in_vehicle", "on_bicycle", "still", "unkown", "tilting", "undefined", "walking", "running"};

        //if (last_log < System.currentTimeMillis()-5000 && activity.getConfidence()>74){
        try {

        // File exist
        if(f.exists()&&!f.isDirectory())
        {
            FileWriter mFileWriter = new FileWriter(filePath, true);
            writer = new CSVWriter(mFileWriter);
        }
        else
        {
            writer = new CSVWriter(new FileWriter(filePath));
        }

        String[] data = {String.valueOf(System.currentTimeMillis()), actCon[activity.getType()], String.valueOf(activity.getConfidence())};

        writer.writeNext(data);

        writer.close();
        //last_log = System.currentTimeMillis();
        }
        catch (Exception Fileexecion){

        }
    }
}
