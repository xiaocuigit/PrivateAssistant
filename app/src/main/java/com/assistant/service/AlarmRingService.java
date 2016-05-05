package com.assistant.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.Vibrator;

import com.assistant.App;
import com.assistant.utils.ConstUtils;
import com.orhanobut.logger.Logger;

public class AlarmRingService extends Service {
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;

    public AlarmRingService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        boolean isVibrate = App.getPreferenceUtils().getBooleanParam(ConstUtils.IS_VIBRATE, false);
        Logger.d("isVibrate = " + isVibrate);
        if (isVibrate) {
            startVibrate();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String mRing = intent.getStringExtra("ringId");
        if (mRing == null) {
            mRing = "everybody.mp3";
        }
        ringTheAlarm(mRing);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTheAlarm();
        stopVibrate();
        Logger.d("关闭后台服务");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void ringTheAlarm(String ring) {
        AssetFileDescriptor assetFileDescriptor = null;
        try {
            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
            }
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            if (ring.contains("/")) {
                mediaPlayer.setDataSource(ring);
            } else {
                assetFileDescriptor = this.getAssets().openFd(ring);
                mediaPlayer.setDataSource(assetFileDescriptor.getFileDescriptor(),
                        assetFileDescriptor.getStartOffset(), assetFileDescriptor.getLength());
            }

            mediaPlayer.setVolume(1f, 1f);
            mediaPlayer.setLooping(true);
            mediaPlayer.prepare();
            mediaPlayer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopTheAlarm() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void startVibrate() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(new long[]{500, 1500, 500, 1500}, 0);
        }
    }

    private void stopVibrate() {
        if (vibrator != null) {
            vibrator.cancel();
        }
    }
}
