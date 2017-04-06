package com.feiyu.audiorecordertest.recorder;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by 李鸿达 on 2017/4/6.
 */

public class RecordView extends ImageView implements AudioManger.AudioStateListener {

    private AudioManger audioManger;

    private boolean isRecording = false;//是否正在录制
    private boolean tooShort = false;//是否时间过短
    private int time = 0;
    private Handler handler;

    private static final int MSG_TIME = 0x001;
    private int MaxVoice = 14;//默认音量等级

    public RecordView(Context context) {
        this(context, null);
    }

    public RecordView(Context context, AttributeSet attrs) {
        super(context, attrs);

        String dir = Environment.getExternalStorageDirectory() + "/audios";
        audioManger = AudioManger.getInstance(dir);
        audioManger.setOnAudioStateListener(this);

        handler = new Handler();
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRecording) {//正在录制的情况下点击，要么是正常结束要么是时间太短
                    if (time < 3) {//少于3秒钟
                        tooShort = true;
                    } else {
                        tooShort = false;
                    }
                    if (tooShort) {
                        //时间过短  删除录音文件  弹出土司提示
                        reset();
                        //回调
                        audioRecordListener.tooShort();
                        audioManger.release();
                    } else {
                        //正常结束
                        audioManger.release();
                        //回调
                        audioRecordListener.finish(time, audioManger.getCurrentFilePath());
                        reset();
                    }
                } else {//开始录音
                    audioManger.prepareAudio();
                }
            }
        });
    }


    //audio准备后之后开始录音的回调
    @Override
    public void wellPrepared() {
        Log.i("LHD", "wellPrepared");
        reset();
        isRecording = true;
        //更新音量
        handler.postDelayed(runnable, 1000);
    }

    //接口回调
    public interface AudioRecordListener {
        //已经录制的时间
        void hasRecord(int seconds);

        //录制完成
        void finish(int seconds, String filePath);

        //时间太短
        void tooShort();

        //音量大小  根据设置的最大音量返回当前音量值
        void curVoice(int voice);
    }

    private AudioRecordListener audioRecordListener;

    public void setOnAudioRecordListener(AudioRecordListener listener) {
        audioRecordListener = listener;
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording) {
                time++;
                audioRecordListener.hasRecord(time);
                audioRecordListener.curVoice(audioManger.getVoiceLevel(MaxVoice));
                handler.postDelayed(this, 1000);
            }
        }
    };

    //置位
    private void reset() {
        isRecording = false;
        time = 0;
        tooShort = false;

        handler.removeCallbacks(runnable);
    }

    //get set 方法
    public void setMaxVoice(int maxVoice) {
        MaxVoice = maxVoice;
    }

    public int getMaxVoice() {
        return MaxVoice;
    }
}
