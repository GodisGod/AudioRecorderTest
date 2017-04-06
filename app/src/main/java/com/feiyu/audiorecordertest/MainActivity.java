package com.feiyu.audiorecordertest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;

import com.feiyu.audiorecordertest.recorder.RecordButton;
import com.feiyu.audiorecordertest.recorder.RecordView;

public class MainActivity extends AppCompatActivity {

    private Button btnRecorder;

    RecordButton recordButton;
    RecordView recordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnRecorder = (Button) findViewById(R.id.btn_recorder);
        recordButton = (RecordButton) findViewById(R.id.record_btn);
        recordView = (RecordView) findViewById(R.id.record_view);
        recordButton.setAudioFinishRecorderListener(new RecordButton.AudioFinishRecorderListener() {
            @Override
            public void onFinish(int seconds, String filePath) {
                Log.i("LHD", "setAudioFinishRecorderListener: " + seconds + "  " + filePath);
            }
        });
        recordButton.setAudioHasRecordListener(new RecordButton.AudioHasRecordListener() {
            @Override
            public void hasRecord(int seconds) {
                Log.i("LHD", "hasRecord: " + seconds);
            }
        });

        recordView.setMaxVoice(16);
        recordView.setOnAudioRecordListener(new RecordView.AudioRecordListener() {
            @Override
            public void hasRecord(int seconds) {
                Log.i("LHD", "hasrecord: " + seconds);
            }

            @Override
            public void finish(int seconds, String filePath) {
                Log.i("LHD", "finish: " + seconds + "  " + filePath);
            }

            @Override
            public void tooShort() {
                Log.i("LHD", "tooShort: ");
            }

            @Override
            public void curVoice(int voice) {
                Log.i("LHD", "voice :  " + voice);
            }
        });

    }

}
