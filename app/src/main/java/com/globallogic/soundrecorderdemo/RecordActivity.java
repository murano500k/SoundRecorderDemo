package com.globallogic.soundrecorderdemo;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_MUSIC;

public class RecordActivity extends AppCompatActivity {
    private static final String TAG = "RecordActivity";
    public static final String KEY_OUTPUT_FILE = "key_out_file";
    private MediaRecorder myAudioRecorder;
    private String outputFilePath;
    private MeterView meterView;
    private FloatingActionButton fabRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);
        meterView = findViewById(R.id.meterView);
        fabRecord = findViewById(R.id.fabRecord);
        fabRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording()){
                    stopRecording();
                }else {
                    startRecording();
                }
            }
        });
        startRecording();
    }

    private boolean isRecording() {
        return meterView.isRecording();
    }

    private void startRecording(){
        try {
            outputFilePath = getOutputFilePath();
            myAudioRecorder = new MediaRecorder();
            myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
            myAudioRecorder.setOutputFile(outputFilePath);
            myAudioRecorder.prepare();
            meterView.setRecorder(myAudioRecorder);
            meterView.setIsRecording(true);
            myAudioRecorder.start();
            fabRecord.setImageResource(R.drawable.ic_stop);
        } catch (IllegalStateException ise) {
            Log.e(TAG, "error: "+ise.getMessage() );
            ise.printStackTrace();
        } catch (IOException ioe) {
            Log.e(TAG, "error: "+ioe.getMessage() );
            ioe.printStackTrace();
        }
    }
    private void stopRecording(){
        meterView.setIsRecording(false);
        myAudioRecorder.stop();
        myAudioRecorder.release();
        myAudioRecorder = null;
        fabRecord.setImageResource(R.drawable.ic_mic);
        Intent resultIntent = new Intent();
        resultIntent.putExtra(KEY_OUTPUT_FILE, outputFilePath);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }



    private String getOutputFilePath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());
        File file = new File(getExternalFilesDir(DIRECTORY_MUSIC).getAbsolutePath() + "/recording"+currentDateTime +".3gp");

        Log.w(TAG, "getOutputFilePath: "+file.getAbsolutePath() );
        return file.getAbsolutePath();
    }

}
