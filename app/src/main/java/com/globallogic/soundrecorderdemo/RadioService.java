package com.globallogic.soundrecorderdemo;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import java.io.File;

public class RadioService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {
    private static final String TAG = "RadioService";
    //media player
    private MediaPlayer player;
    //song list
    //current position
    private final IBinder musicBind = new RadioServiceBinder();
    private File mCurrentFile;

    public void onCreate(){
        //create the service
        super.onCreate();
        //initialize position
        //create player
        player = new MediaPlayer();
        initMusicPlayer();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void initMusicPlayer(){
        player.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnPreparedListener(this);
        player.setOnCompletionListener(this);
        player.setOnErrorListener(this);
    }

    public boolean isPlaying() {
        return player!=null && player.isPlaying();
    }

    public File getCurrentFile(){
        return mCurrentFile;
    }

    public class RadioServiceBinder extends Binder {
        RadioService getService() {
            return RadioService.this;
        }
    }
    public void playSong(File file){
        mCurrentFile = file;
        player.reset();
        //get song


        try {
            player.setDataSource(file.getAbsolutePath());
        }
        catch(Exception e){
            Log.e(TAG, "Error setting data source", e);
        }
        player.prepareAsync();
    }

    public void pauseSong(){
        if(player.isPlaying()){
            player.pause();
        }
        mCurrentFile=null;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return musicBind;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        player.stop();
        player.release();
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.w(TAG, "onCompletion: " );
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.w(TAG, "onError: " );
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

}

