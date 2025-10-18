package com.clem.burgerbox;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;

public class SoundPlayer {

    private static SoundPlayer instance;
    private final SoundPool soundPool;
    private final SparseIntArray soundIds;
    private final SparseBooleanArray toAutoPlay;
    private final Context appContext;

    private SoundPlayer(Context context) {
        appContext = context.getApplicationContext();
        AudioAttributes attrs = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)                // <- au lieu de USAGE_ASSISTANCE_SONIFICATION
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attrs)
                .setMaxStreams(8)
                .build();
        soundIds = new SparseIntArray();
        toAutoPlay = new SparseBooleanArray();

        // évite qu’un premier tap soit muet si le chargement termine avant l’installation du listener
        soundPool.setOnLoadCompleteListener((sp, sampleId, status) -> {
            if (status == 0 && toAutoPlay.get(sampleId, false)) {
                toAutoPlay.delete(sampleId);
                sp.play(sampleId, 1f, 1f, 1, 0, 1f);
            }
        });
    }

    public static synchronized SoundPlayer get(Context context) {
        if (instance == null) instance = new SoundPlayer(context);
        return instance;
    }

    // charge et joue quand prêt
    public void play(int resId) {
        int soundId = soundIds.get(resId, -1);
        if (soundId == -1) {
            soundId = soundPool.load(appContext, resId, 1);
            soundIds.put(resId, soundId);
            toAutoPlay.put(soundId, true); // auto-play dès que prêt
        } else {
            soundPool.play(soundId, 1f, 1f, 1, 0, 1f);
        }
    }

    public void release() {
        soundPool.release();
        instance = null;
    }
}
