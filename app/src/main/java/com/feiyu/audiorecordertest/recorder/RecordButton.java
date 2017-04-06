package com.feiyu.audiorecordertest.recorder;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.feiyu.audiorecordertest.R;


/**
 * Created by ${鸿达} on 2016/8/30.
 */
public class RecordButton extends ImageView implements AudioManger.AudioStateListener {

    private static final int STATE_NORMAL = 1;
    private static final int STATE_RECORDING = 2;

    private int mCurSate = STATE_NORMAL;
    private boolean isRecording = false;
    private AudioManger mAudioManger;

    //计算录音时间
    private float mTime;
    //是否触发longclick
//    private boolean mReady;

    //最大录音时间
    private int maxRecordTime = 60;

    private Runnable mGetVoiceLevelRunnable = new Runnable() {
        @Override
        public void run() {
            while (isRecording) {
                try {
                    Thread.sleep(100);
                    mTime += 0.1f;
                    Log.i("LHD", "mTime: " + mTime);
                    int b = (int) Math.round(mTime + 0.5);//保留两位小数
                    if (b == maxRecordTime) {
                        Log.i("LHD", "finished");
                        mHandler.sendEmptyMessage(MSG_FINISHED);
                        isRecording = false;
                        //因为handler处理消息的速度比线程慢，所以要及时在这里将isRecording赋值，而不是在handle中处理，否则还会进入这个while循环。
                    } else {
                        mHandler.sendEmptyMessage(MSG_HAS_FINISHED);
                        mHandler.sendEmptyMessage(MSG_VOICE_CHANGED);
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static final int MSG_AUDIO_PREPARED = 0X110;
    private static final int MSG_VOICE_CHANGED = 0X111;
    private static final int MSG_HAS_FINISHED = 0X112;
    private static final int MSG_FINISHED = 0X113;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_AUDIO_PREPARED:
                    isRecording = true;
                    new Thread(mGetVoiceLevelRunnable).start();
                    break;
                case MSG_VOICE_CHANGED:
                    //更新音量
                    updateVoiceLevel(mAudioManger.getVoiceLevel(14));
                    break;
                case MSG_HAS_FINISHED:
                    //更新时间
                    if (mAudioHasRecordListener != null) {
                        int b = (int) Math.round(mTime + 0.5);//保留两位小数
                        mAudioHasRecordListener.hasRecord(b);
                    }
                    break;
                case MSG_FINISHED:
                    //结束
                    if (mFinishListener != null) {
                        int b = (int) (Math.round(mTime + 0.5));//保留两位小数
                        mFinishListener.onFinish(b, mAudioManger.getCurrentFilePath());
                    }
                    mAudioManger.release();
                    reset();
                    break;
            }
        }
    };

    public RecordButton(Context context) {
        this(context, null);
    }

    private AudioFinishRecorderListener mFinishListener;
    private AudioHasRecordListener mAudioHasRecordListener;

    //录音完成后的回调
    public interface AudioFinishRecorderListener {
        void onFinish(int seconds, String filePath);
    }

    //已经录音时间的回调
    public interface AudioHasRecordListener {
        void hasRecord(int seconds);
    }

    public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener) {
        mFinishListener = listener;
    }

    public void setAudioHasRecordListener(AudioHasRecordListener audioHasRecordListener) {
        mAudioHasRecordListener = audioHasRecordListener;
    }

    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        String dir = Environment.getExternalStorageDirectory() + "/scrip_audios";//注意加上斜杠
        mAudioManger = AudioManger.getInstance(dir);
        mAudioManger.setOnAudioStateListener(this);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecording) {
//                    mReady = true;
                    mAudioManger.prepareAudio();
                    setBackgroundResource(R.mipmap.ic_launcher);
                    changeState(STATE_RECORDING);
                } else {
//                    if (!mReady) {
//                        reset();
//                        return;
//                    }
                    if (mCurSate == STATE_RECORDING) {//正常录制结束
                        mAudioManger.release();
                        if (mFinishListener != null) {
                            int b = (int) (Math.round(mTime + 0.5));//保留两位小数
                            mFinishListener.onFinish(b, mAudioManger.getCurrentFilePath());
                        }
                        isRecording = false;
                    }
                    reset();
                }
            }
        });
    }

    @Override
    public void wellPrepared() {
        mHandler.sendEmptyMessage(MSG_AUDIO_PREPARED);
    }


    //恢复一些标志位
    private void reset() {
        isRecording = false;
//        mReady = false;
        mTime = 0;
        changeState(STATE_NORMAL);
    }


    private void changeState(int stateRecording) {
        if (mCurSate != stateRecording) {
            mCurSate = stateRecording;
        }
    }

    //更新音量.通过level去更新voice上的图片
    public void updateVoiceLevel(int level) {
        int resId;
        if (level < 10) {
            resId = getContext().getResources().getIdentifier("record_animate_0" + level, "drawable",
                    getContext().getPackageName());
        } else {
            resId = getContext().getResources().getIdentifier("record_animate_" + level, "drawable",
                    getContext().getPackageName());
        }
//        setImageResource(resId);
        setBackgroundResource(resId);
    }
}
