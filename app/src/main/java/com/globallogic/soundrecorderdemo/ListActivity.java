package com.globallogic.soundrecorderdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.os.Environment.DIRECTORY_MUSIC;

public class ListActivity extends AppCompatActivity implements MyListAdapter.OnSelectedItemChangedListener {
    private static final String TAG = "ListActivity";
    private static final int MY_PERMISSIONS_REQUEST = 2523;
    private static final int RECORD_SOUND_REQUEST = 2433;

    private RecyclerView mRecycleView;
    private MyListAdapter mAdapter;
    private FloatingActionButton mFabRecord, mFabPlay;
    private MediaPlayer mMediaPlayer;
    private SeekBar mSeekBar;
    private Runnable mRunnable;
    private Handler mHandler;
    private TextView mTextPassedTime, mTextLeftTime;
    private File mCurrentFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        mFabRecord = findViewById(R.id.fabRecord);
        mFabPlay = findViewById(R.id.fabPlay);
        mRecycleView =findViewById(R.id.list);
        mSeekBar = findViewById(R.id.seekBar);
        mTextPassedTime = findViewById(R.id.textPassedTime);
        mTextLeftTime = findViewById(R.id.textLeftTime);
        mHandler = new Handler();

        if (ContextCompat.checkSelfPermission(this,
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
        }
    }

    private void initComponents() {

        initFab();
        initList();
        initMediaPlayer();
        updateSeekBarVisibility(false);
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                //textStatus.setText("Ready");
                Log.w(TAG, "onCompletion: " );
                mFabPlay.setImageResource(android.R.drawable.ic_media_play);
                updateSeekBarVisibility(false);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RECORD_SOUND_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                String recordedFile = data.getStringExtra(RecordActivity.KEY_OUTPUT_FILE);
                Log.w(TAG, "onActivityResult: "+recordedFile );
                initList();
                selectItem(recordedFile);
            }
        }
    }

    private void selectItem(String recordedFile) {
        mRecycleView.smoothScrollToPosition(mAdapter.setSelectedItem(recordedFile));
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

    private void initFab() {
        mFabRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(),RecordActivity.class);
                startActivityForResult(intent, RECORD_SOUND_REQUEST);
            }
        });

        mFabPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w(TAG, "onClick: mFabPlay" );
                if(!isPlaying()){
                    playFile(mAdapter.getSelectedItem());
                }else {
                    pausePlayback();

                }
            }
        });
    }

    private boolean isPlaying() {
        return mMediaPlayer!=null && mMediaPlayer.isPlaying();
    }

    private void initList() {
        mAdapter =new MyListAdapter(getTracks(),this);
        mAdapter.setOnSelectedItemChangedListener(this);

        mRecycleView.setLayoutManager(new LinearLayoutManager(this));
        mRecycleView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
    }


    private List<File> getTracks(){
        File dir = getExternalFilesDir(DIRECTORY_MUSIC);
        if(dir!=null && dir.listFiles()!=null){
            Log.w(TAG, "getTracks: "+dir.listFiles().length);
            return Arrays.asList(dir.listFiles());
        }else {
            Log.w(TAG, "getTracks: 0" );
            return new ArrayList<>();
        }
    }

    @Override
    public void onSelectedItemChanged(File file) {
        Log.d(TAG, "onSelectedItemChanged() called with: file = [" + file + "]");
        playFile(file);
    }

    private void playFile(File file){
        Log.d(TAG, "playFile() called with: file = [" + file + "]");
        mFabPlay.setImageResource(android.R.drawable.ic_media_pause);
        if(mCurrentFile!=null && mCurrentFile.getAbsolutePath().contains(file.getAbsolutePath())){
            mMediaPlayer.start();
        }else {
            try {
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(file.getAbsolutePath());
                mMediaPlayer.prepare();
                mMediaPlayer.start();
                Log.w(TAG, "playFile");
                //textStatus.setText("Playing Audio");
                //Toast.makeText(getApplicationContext(), "Playing Audio", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "onClick: error"+e.getMessage() );
            }
        }
        getAudioStats();
        // Initialize the seek bar
        initializeSeekBar();
        mCurrentFile = file;

    }
    private void pausePlayback(){
        Log.d(TAG, "pausePlayback() called");
        if(mMediaPlayer!=null && mMediaPlayer.isPlaying()){
            mMediaPlayer.pause();
        }
        mFabPlay.setImageResource(android.R.drawable.ic_media_play);
        updateSeekBarVisibility(false);
        if(mHandler!=null){
            mHandler.removeCallbacks(mRunnable);
        }
    }

    protected void initializeSeekBar(){
        updateSeekBarVisibility(true);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(mMediaPlayer!=null && b){
                    mMediaPlayer.seekTo(i*1000);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        if(mMediaPlayer!=null) {
            mSeekBar.setMax(mMediaPlayer.getDuration() / 1000);

            mRunnable = new Runnable() {
                @Override
                public void run() {
                    if (mMediaPlayer != null) {
                        int mCurrentPosition = mMediaPlayer.getCurrentPosition() / 1000; // In milliseconds
                        mSeekBar.setProgress(mCurrentPosition);
                        getAudioStats();
                    }
                    mHandler.postDelayed(mRunnable, 300);
                }
            };
            mHandler.postDelayed(mRunnable, 300);
        }
    }
    protected void getAudioStats(){
        int duration  = mMediaPlayer.getDuration()/1000; // In milliseconds
        int due = (mMediaPlayer.getDuration() - mMediaPlayer.getCurrentPosition())/1000;
        int pass = duration - due;
        mTextLeftTime.setText(secondsToString(due));
        mTextPassedTime.setText(secondsToString(pass));
    }
    private String secondsToString(int pTime) {
        return String.format("%02d:%02d", pTime / 60, pTime % 60);
    }

    private void updateSeekBarVisibility(boolean visible){
        /*if(!visible){
            mSeekBar.setProgress(0);
            mTextLeftTime.setText(secondsToString(0));
            mTextPassedTime.setText(secondsToString(0));
        }
        int visibility = visible ? View.VISIBLE : View.GONE;
        mSeekBar.setVisibility(visibility);
        mTextPassedTime.setVisibility(visibility);
        mTextLeftTime.setVisibility(visibility);*/

    }

}
