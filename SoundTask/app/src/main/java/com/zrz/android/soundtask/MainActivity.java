package com.zrz.android.soundtask;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button buttonRecord, buttonPlay;
    private MediaRecorder recorder;
    private MediaPlayer player;
    private String filePath;
    private static final int REQUEST = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonRecord=(Button)findViewById(R.id.button_record);
        buttonPlay=(Button)findViewById(R.id.button_play);
        buttonRecord.setOnClickListener(this);
        buttonPlay.setOnClickListener(this);

        filePath=getFilePath();
        confirmPermissions(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_record:
                MyRecordTask myRecordTask=new MyRecordTask(this);
                myRecordTask.execute();
                Toast.makeText(this,getResources().getText(R.string.button_record),Toast.LENGTH_SHORT).show();
                break;
            case R.id.button_play:
                MyPlayTask myPlayTask=new MyPlayTask(this);
                myPlayTask.execute();
                Toast.makeText(this,getResources().getText(R.string.button_play),Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    private void record(){
        try{
            recorderRelease();
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(filePath);
            recorder.prepare();
            recorder.start();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void play(){
        try {
            playerRelease();
            player = new MediaPlayer();
            player.setDataSource(filePath);
            player.prepare();
            player.start();
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    player.release();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recorderRelease() {
        if (recorder != null) {
            recorder.release();
        }
    }

    private void playerRelease() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private String getFilePath(){
        String fileName = "my_record.3gpp";
        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        filePath = baseDir+File.separator+fileName;
        return filePath;
    }

    private static class MyRecordTask extends AsyncTask<Void, Integer, Void> {
        private WeakReference<MainActivity> mainActivityReference;
        MyRecordTask(MainActivity context){
            mainActivityReference=new WeakReference<>(context);
        }
        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < 10; i++) {
                publishProgress(i);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(i==9){
                    publishProgress(i+1);
                }
            }
            return null;
        }
        @Override
        protected void onProgressUpdate (Integer... steps) {
            super.onProgressUpdate(steps);
            MainActivity mainActivity=mainActivityReference.get();
            if(mainActivity.isDestroyed()||mainActivity.isFinishing()){
                return;
            }
            for(int step:steps) {
                if(step==0){
                    mainActivity.record();
                }
                mainActivity.buttonRecord.setText(Integer.toString(step));
                if(step==10){
                    mainActivity.buttonRecord.setText(R.string.button_record);
                    mainActivity.recorder.stop();
                    mainActivity.recorder.release();
                }
            }
        }
    }

    private static class MyPlayTask extends AsyncTask<Void, Void, Void> {
        private WeakReference<MainActivity> mainActivityReference;
        MyPlayTask(MainActivity context){
            mainActivityReference=new WeakReference<>(context);
        }
        @Override
        protected Void doInBackground(Void... voids) {
            publishProgress();
            return null;
        }
        @Override
        protected void onProgressUpdate (Void... voids) {
            super.onProgressUpdate();
            MainActivity mainActivity=mainActivityReference.get();
            if(mainActivity.isDestroyed()||mainActivity.isFinishing()){
                return;
            }
            mainActivity.play();
        }
    }

    public static void confirmPermissions(MainActivity mainActivity) {
        int permission = ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mainActivity, PERMISSIONS_STORAGE, REQUEST);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        playerRelease();
        recorderRelease();
    }
}
