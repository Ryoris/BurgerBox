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

public class Generer_son extends BaseActivity {

    // --- Réglages ---
    private static final float RATE_MIN  = 0.05f;  // ex: 0.05x
    private static final float RATE_MAX  = 3.00f;  // ex: 3.00x
    private static final float PITCH_MIN = 0.25f;  // ex: 0.25x
    private static final float PITCH_MAX = 3.00f;  // ex: 3.00x

    private TextToSpeech tts;
    private Button btnPlay, btnSave;
    private EditText inputText;
    private SeekBar seekRate, seekPitch;
    private TextView tvRateLabel, tvPitchLabel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityLayout(R.layout.generer_son);

        // --- Id du xml ---
        btnPlay      = findViewById(R.id.btnPlay);
        btnSave      = findViewById(R.id.btnSave);
        inputText    = findViewById(R.id.inputText);
        seekRate     = findViewById(R.id.seekRate);
        seekPitch    = findViewById(R.id.seekPitch);
        tvRateLabel  = findViewById(R.id.tvRateLabel);
        tvPitchLabel = findViewById(R.id.tvPitchLabel);

        // --- SeekBars paramètres ---
        seekRate.setMax(200);
        seekPitch.setMax(200);
        seekRate.setProgress(valueToProgress(1.0f, RATE_MIN, RATE_MAX));
        seekPitch.setProgress(valueToProgress(1.0f, PITCH_MIN, PITCH_MAX));
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

        // --- TTS (Text-to-Speech) ---
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int res = tts.setLanguage(Locale.FRANCE);
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "La langue FR n'est pas supportée par le moteur TTS.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Échec de l'initialisation du TTS.", Toast.LENGTH_LONG).show();
            }
        });

        // --- Actions ---
        btnPlay.setOnClickListener(v -> playNow());
        btnSave.setOnClickListener(v -> saveToDownloadsWav());
    }

    // --- Ajustement de la barre en fonction de la progression ---
    private float progressToValue(int progress, float min, float max) {
        return min + (progress / 200f) * (max - min);
    }
    private int valueToProgress(float value, float min, float max) {
        if (value < min) value = min;
        if (value > max) value = max;
        return Math.round(((value - min) / (max - min)) * 200f);
    }


    // --- Lecture du son ---
    private void playNow() {
        if (tts == null) { toast("TTS non prêt."); return; }

        String text = safeText();
        if (text.isEmpty()) { toast("Entrez du texte à dire."); return; }

        float rate  = progressToValue(seekRate.getProgress(), RATE_MIN, RATE_MAX);
        float pitch = progressToValue(seekPitch.getProgress(), PITCH_MIN, PITCH_MAX);
        tts.setSpeechRate(rate);
        tts.setPitch(pitch);

        String utteranceId = "play-" + System.currentTimeMillis();
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override public void onStart(String id) {}
            @Override public void onDone(String id) {}
            @Override public void onError(String id) { runOnUiThread(() -> toast("Erreur de lecture.")); }
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

            @Override public void onDone(String id) {
                // Copie vers Téléchargements via MediaStore (Android 10+)
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
                            while ((len = fis.read(buf)) != -1) {
                                os.write(buf, 0, len);
                            }
                            os.flush();
                        }

                        // Nettoie le tmp
                        tmp.delete();

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

    // --- Sécurise le texte ---
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
}
