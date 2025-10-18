package com.clem.burgerbox;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.SparseIntArray;

public class SoundPlayer {

    private static SoundPlayer instance;
    private final SoundPool soundPool;
    private final SparseIntArray soundIds;
    private final Context appContext;

    private SoundPlayer(Context context) {
        appContext = context.getApplicationContext();
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attrs)
                .setMaxStreams(8)
                .build();
        soundIds = new SparseIntArray();
    }

    public static synchronized SoundPlayer get(Context context) {
        if (instance == null) instance = new SoundPlayer(context);
        return instance;
    }

    public void play(int resId) {
        int spId = soundIds.get(resId, -1);
        if (spId == -1) {
            // charge et joue quand prêt
            spId = soundPool.load(appContext, resId, 1);
            final int finalId = spId;
            soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
                if (status == 0 && sampleId == finalId) {
                    sp.play(finalId, 1f, 1f, 1, 0, 1f);
                }
            });
            soundIds.put(resId, spId);
        } else {
            soundPool.play(spId, 1f, 1f, 1, 0, 1f);
        }
    }

    public void release() {
        soundPool.release();
        instance = null;
    }
}
