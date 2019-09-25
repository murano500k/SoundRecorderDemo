package com.globallogic.soundrecorderdemo;

import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.os.Environment.DIRECTORY_MUSIC;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST = 444;
    private Button btnPlay, btnStop, btnRecord;
    private TextView textStatus;
    private TextView mPass,mDuration,mDue;
    private MediaRecorder myAudioRecorder;
    private String outputFilePath;
    private static final String TAG = "MainActivity";
    private MediaPlayer mediaPlayer;
    private SeekBar seekBar;
    private Runnable mRunnable;
    private Handler mHandler;

    private MeterView meterView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnPlay = (Button) findViewById(R.id.play);
        btnStop = (Button) findViewById(R.id.stop);
        btnRecord = (Button) findViewById(R.id.record);
        seekBar = findViewById(R.id.seekBar);
        textStatus = findViewById(R.id.textStatus);
        meterView = findViewById(R.id.meterView);
        mPass=findViewById(R.id.mPass);
        mDuration=findViewById(R.id.mDuration);
        mDue=findViewById(R.id.mDue);

        mHandler = new Handler();
        btnStop.setEnabled(false);
        btnPlay.setEnabled(false);
        /*if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);
        } else {
            initComponents();
        }*/
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initComponents();
                } else {
                    Toast.makeText(this, "No permission", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void initComponents() {
        meterView.setRecorder(myAudioRecorder);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(mediaPlayer!=null && b){
                    mediaPlayer.seekTo(i*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        btnRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                } catch (IllegalStateException ise) {
                    Log.e(TAG, "error: "+ise.getMessage() );
                    ise.printStackTrace();
                } catch (IOException ioe) {
                    Log.e(TAG, "error: "+ioe.getMessage() );
                    ioe.printStackTrace();
                }
                btnRecord.setEnabled(false);
                btnStop.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
                textStatus.setText("Recording...");
            }
        });

        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                meterView.setIsRecording(false);
                myAudioRecorder.stop();
                myAudioRecorder.release();
                myAudioRecorder = null;
                if(mHandler!=null){
                    mHandler.removeCallbacks(mRunnable);
                }
                btnRecord.setEnabled(true);
                btnStop.setEnabled(false);
                btnPlay.setEnabled(true);
                Toast.makeText(getApplicationContext(), "Audio Recorder successfully", Toast.LENGTH_LONG).show();
                textStatus.setText("Record saved: "+outputFilePath);
            }
        });

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        textStatus.setText("Ready");
                    }
                });
                try {
                    mediaPlayer.setDataSource(outputFilePath);
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                    textStatus.setText("Playing Audio");
                    Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "onClick: error"+e.getMessage() );
                }
                getAudioStats();
                // Initialize the seek bar
                initializeSeekBar();
            }
        });
    }

    private String getOutputFilePath() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
        String currentDateTime = sdf.format(new Date());
        File file = new File(getExternalFilesDir(DIRECTORY_MUSIC).getAbsolutePath() + "/recording"+currentDateTime +".3gp");

        Log.w(TAG, "getOutputFilePath: "+file.getAbsolutePath() );
        return file.getAbsolutePath();
    }


    protected void initializeSeekBar(){
        seekBar.setMax(mediaPlayer.getDuration()/1000);

        mRunnable = new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer!=null){
                    int mCurrentPosition = mediaPlayer.getCurrentPosition()/1000; // In milliseconds
                    seekBar.setProgress(mCurrentPosition);
                    getAudioStats();
                }
                mHandler.postDelayed(mRunnable,1000);
            }
        };
        mHandler.postDelayed(mRunnable,1000);
    }
    protected void getAudioStats(){
        int duration  = mediaPlayer.getDuration()/1000; // In milliseconds
        int due = (mediaPlayer.getDuration() - mediaPlayer.getCurrentPosition())/1000;
        int pass = duration - due;

        mPass.setText("" + pass + " seconds");
        mDuration.setText("" + duration + " seconds");
        mDue.setText("" + due + " seconds");
    }
}