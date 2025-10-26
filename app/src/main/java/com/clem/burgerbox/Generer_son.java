package com.clem.burgerbox;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.Locale;

// Imports nécessaires : TTS, MediaStore (sauvegarde dans Téléchargements), UI, I/O, Locale.

public class Generer_son extends BaseActivity {

    // --- Réglages ---
    private static final float RATE_MIN  = 0.05f;  // Vitesse minimale 0.05x (très lent)
    private static final float RATE_MAX  = 3.00f;  // Vitesse max 3x
    private static final float PITCH_MIN = 0.25f;  // Hauteur minimale 0.25x
    private static final float PITCH_MAX = 3.00f;  // Hauteur max 3x
    private TextToSpeech tts;                      // Moteur TTS
    private boolean ttsReady = false;

    private EditText inputText;                    // Champ de texte
    private SeekBar seekRate, seekPitch;           // Sliders vitesse & hauteur
    private TextView tvRateLabel, tvPitchLabel;    // Libellés

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityLayout(R.layout.generer_son);

        // --- Id du xml ---
        Button btnPlay = findViewById(R.id.btnPlay);
        Button btnSave = findViewById(R.id.btnSave);
        inputText    = findViewById(R.id.inputText);
        seekRate     = findViewById(R.id.seekRate);
        seekPitch    = findViewById(R.id.seekPitch);
        tvRateLabel  = findViewById(R.id.tvRateLabel);
        tvPitchLabel = findViewById(R.id.tvPitchLabel);

        // --- SeekBars paramètres ---
        // Les SeekBars vont de 0 à 200. Position initiale = 1.0x
        seekRate.setMax(200);
        seekPitch.setMax(200);
        seekRate.setProgress(valueToProgress(RATE_MIN, RATE_MAX));
        seekPitch.setProgress(valueToProgress(PITCH_MIN, PITCH_MAX));
        tvRateLabel.setText(String.format(Locale.US, "Vitesse : %.2fx", progressToValue(seekRate.getProgress(), RATE_MIN, RATE_MAX)));
        tvPitchLabel.setText(String.format(Locale.US, "Pitch : %.2fx", progressToValue(seekPitch.getProgress(), PITCH_MIN, PITCH_MAX)));

        // --- SeekBars vitesse tracking ---
        seekRate.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float rate = progressToValue(progress, RATE_MIN, RATE_MAX);
                tvRateLabel.setText(String.format(Locale.US, "Vitesse : %.2fx", rate));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // --- SeekBars Pitch tracking ---
        seekPitch.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float pitch = progressToValue(progress, PITCH_MIN, PITCH_MAX);
                tvPitchLabel.setText(String.format(Locale.US, "Pitch : %.2fx", pitch));
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Initialise le moteur TTS (Text-to-Speech) et tente de régler la langue sur français (France).

        setUiEnabled(false); // Désactive Play/Save tant que pas prêt

        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int res = tts.setLanguage(Locale.FRENCH); // ou new Locale("fr","FR")
                if (res >= 0) {
                    ttsReady = true;
                    setUiEnabled(true);
                } else {
                    toast("Français non supporté par le moteur TTS.");
                }
            } else {
                toast("Échec de l'initialisation du TTS.");
            }
        });

        // --- Actions : lecture immédiate ou synthèse vers fichier ---
        btnPlay.setOnClickListener(v -> {
            if (!ttsReady) { toast("TTS non prêt."); return; }
            playNow();
        });
        btnSave.setOnClickListener(v -> {
            if (!ttsReady) { toast("TTS non prêt."); return; }
            saveToDownloadsWav();
        });
    }

    private void setUiEnabled(boolean enabled) {
        findViewById(R.id.btnPlay).setEnabled(enabled);
        findViewById(R.id.btnSave).setEnabled(enabled);
    }

    // --- Ajustement de la barre en fonction de la progression ---
    private float progressToValue(int progress, float min, float max) {
        return min + (progress / 200f) * (max - min);
    }
    private int valueToProgress(float min, float max) {
        float value = 1.0f;
        if (value < min) value = min;
        if (value > max) value = max;
        return Math.round(((value - min) / (max - min)) * 200f);
    }


    // --- Lecture du son avec les réglages---
    private void playNow() {
        if (tts == null) { toast("TTS non prêt."); return; }

        String text = safeText();
        if (text.isEmpty()) { toast("Entrez du texte à dire."); return; }

        float rate  = progressToValue(seekRate.getProgress(), RATE_MIN, RATE_MAX);
        float pitch = progressToValue(seekPitch.getProgress(), PITCH_MIN, PITCH_MAX);
        tts.setSpeechRate(rate);
        tts.setPitch(pitch);

        // Installe un listener (basique) pour les callbacks.
        String utteranceId = "play-" + System.currentTimeMillis();
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) {}
            @Override public void onDone(String id) {
                if (id.startsWith("save-")) {
                    runOnUiThread(() -> toast("Fichier prêt (copie en cours si Android 10+)."));
                }
            }
            @Override public void onError(String id) {
                runOnUiThread(() -> toast(id.startsWith("save-") ? "Erreur de synthèse." : "Erreur de lecture."));
            }
        });

        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }


    // --- Enregistrement du son dans le dossier Téléchargements ---
    private void saveToDownloadsWav() {
        if (tts == null) { toast("TTS non prêt."); return; }

        String text = safeText();
        if (text.isEmpty()) { toast("Entrez du texte à dire."); return; }

        // Appliquer les réglages utilisateur
        float rate  = progressToValue(seekRate.getProgress(), RATE_MIN, RATE_MAX);
        float pitch = progressToValue(seekPitch.getProgress(), PITCH_MIN, PITCH_MAX);
        tts.setSpeechRate(rate);
        tts.setPitch(pitch);

        final String baseName = "tts_" + System.currentTimeMillis() + ".wav";
        File tmp = new File(getCacheDir(), baseName);

        String uttId = "save-" + System.currentTimeMillis();
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) {}

            // Fait une synthèse vers un fichier temporaire, puis copie le fichier vers Downloads via MediaStore (Android 10+).
            @Override public void onDone(String id) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        ContentResolver cr = getContentResolver();
                        ContentValues cv = new ContentValues();
                        cv.put(MediaStore.Downloads.DISPLAY_NAME, baseName);
                        cv.put(MediaStore.Downloads.MIME_TYPE, "audio/wav");
                        cv.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                        Uri uri = cr.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                        if (uri == null) throw new Exception("Insertion MediaStore échouée");

                        try (OutputStream os = cr.openOutputStream(uri);
                             FileInputStream fis = new FileInputStream(tmp)) {
                            byte[] buf = new byte[8192];
                            int len;
                            if (os != null) {
                                while ((len = fis.read(buf)) != -1) {
                                    os.write(buf, 0, len);
                                }
                                os.flush();
                            }
                        }
                        safeDelete(tmp);    // Nettoie le tmp
                        runOnUiThread(() -> toast("WAV enregistré dans Téléchargements : " + baseName));
                    } catch (Exception e) {
                        runOnUiThread(() -> toast("Échec de copie vers Téléchargements."));
                    }
                } else {
                    runOnUiThread(() -> toast("Android 9 ou moins : nécessite des permissions pour Téléchargements. Fichier temp : " + tmp.getAbsolutePath()));
                }
            }
            @Override public void onError(String id) {
                runOnUiThread(() -> toast("Erreur de synthèse."));
            }
        });

        int res = tts.synthesizeToFile(text, null, tmp, uttId);
        if (res != TextToSpeech.SUCCESS) {
            toast("Impossible de lancer la synthèse.");
        }
    }

    // --- Évite NullPointer et supprime les espaces.---
    private String safeText() {
        return inputText.getText() == null ? "" : inputText.getText().toString().trim();
    }

    // --- Message à l'utilisateur ---
    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    // --- Arrete le son ---
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
    }
    private void safeDelete(File f) {
        if (f == null) return;
        boolean ok = f.delete();
        if (!ok && f.exists()) {
            toast("Impossible de supprimer le fichier.");
        }
    }
}
