package com.clem.burgerbox;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.net.Uri;
import android.util.SparseBooleanArray;
import android.util.SparseIntArray;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class SoundPlayer {

    private static SoundPlayer instance;    //instance unique
    private final SoundPool soundPool;      // moteur de lecteur
    private final SparseIntArray soundIds;      //map des id de sons
    private final SparseBooleanArray toAutoPlay;
    private final Context appContext;
    private final Map<String, Integer> uriSoundIds = new HashMap<>();


    private SoundPlayer(Context context) {
        appContext = context.getApplicationContext();
        AudioAttributes attrs = new AudioAttributes.Builder()     // décrit à quoi sert le son
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)     // Type de contenu
                .build();

        // SoundPool est une classe Android prévue pour jouer des sons courts
        soundPool = new SoundPool.Builder()
                .setAudioAttributes(attrs)
                .setMaxStreams(8)            // Nombre max de sons simultanés
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

    // Méthode d’accès singleton
    public static synchronized SoundPlayer get(Context context) {
        if (instance == null) instance = new SoundPlayer(context);
        return instance;
    }

    // Charge un son par resId et le joue quand il est prêt
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

    // Variante : joue un son pointé par une Uri (fichier, MediaStore, etc.)
    public void play(Uri uri) {
        String key = uri.toString();
        Integer existing = uriSoundIds.get(key);
        if (existing != null) {
            soundPool.play(existing, 1f, 1f, 1, 0, 1f);
            return;
        }
        // Pas encore chargé : on ouvre un descripteur de fichier sur l’URI
        try (AssetFileDescriptor afd = appContext.getContentResolver()
                .openAssetFileDescriptor(uri, "r")) {
            if (afd == null) return;
            int soundId = soundPool.load(afd, 1);
            uriSoundIds.put(key, soundId);
            toAutoPlay.put(soundId, true);
        } catch (Exception e) {
            Toast.makeText(appContext, "Impossible de charger ce son", Toast.LENGTH_SHORT).show();
        }
    }

    // Libère les ressources natives de SoundPool
    public void release() {
        soundPool.release();
        instance = null;
    }
}
