package com.feiyu.audiorecordertest.recorder;

import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;

/**
 * Created by ${鸿达} on 2016/8/30.
 */
public class AudioManger {
    private MediaRecorder mMediaRecorder;
    private String mDir;
    private String mCurrentFilePath;

    private static AudioManger mInstance;

    private boolean isPrepared;

    //Recorder准备好录音后的回调
    public interface AudioStateListener {
        void wellPrepared();
    }

    public AudioStateListener mListener;

    public void setOnAudioStateListener(AudioStateListener Listener) {
        this.mListener = Listener;
    }


    //单例模式
    public static AudioManger getInstance(String dir) {
        if (mInstance == null) {
            synchronized (AudioManger.class) {
                if (mInstance == null) {
                    mInstance = new AudioManger(dir);
                }
            }
        }
        return mInstance;
    }

    //构造器 参数:  录音文件夹
    private AudioManger(String dir) {
        mDir = dir;
    }

    //准备录音
    public void prepareAudio() {
        try {
            isPrepared = false;
            //在录音目录下建立录音文件
            File dir = new File(mDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = generateFileName();
            File file = new File(dir, fileName);
            mCurrentFilePath = file.getAbsolutePath();

            mMediaRecorder = new MediaRecorder();
            //设置输出文件
            mMediaRecorder.setOutputFile(mCurrentFilePath);
            //设置音频源为麦克风
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置音频格式api>=10使用amr_nb 小于10使用raw_amr
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            //设置音频的编码为amr
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            //准备录制
            mMediaRecorder.prepare();
            mMediaRecorder.start();

            //准备好录制
            isPrepared = true;
            if (mListener != null) {
                mListener.wellPrepared();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //随机生成文件名称
    private String generateFileName() {
        return System.currentTimeMillis() + ".amr";
    }

    //获取音量大小  maxLevel为最大等级
    public int getVoiceLevel(int maxLevel) {
        if (isPrepared) {
            try {
                //mMediaRecorder.getMaxAmplitude() 1-32767
                return maxLevel * mMediaRecorder.getMaxAmplitude() / 32768 + 1;
            } catch (Exception e) {

            }
        }
        return 1;
    }

    public void release() {
        mMediaRecorder.stop();
        mMediaRecorder.release();
        mMediaRecorder = null;
    }

    public void cancel() {
        release();
        //如果取消就删除生成的录音文件
        if (mCurrentFilePath != null) {
            File file = new File(mCurrentFilePath);
            file.delete();
            mCurrentFilePath = null;
        }
    }

    public String getCurrentFilePath() {
        return mCurrentFilePath;
    }

}
