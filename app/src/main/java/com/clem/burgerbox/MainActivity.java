package com.clem.burgerbox;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.ListView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    private ActivityResultLauncher<Intent> pickAudioLauncher;
    private ArrayList<Audio> soundsList;              // rends-le champ d’instance
    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityLayout(R.layout.activity_main);

        // Liste de sons par défaut
        soundsList = new ArrayList<>();
        soundsList.add(new Audio("big_bell"));
        soundsList.add(new Audio("fart1"));
        soundsList.add(new Audio("throwing"));

        ArrayList<TwoAudio> twoSoundsList = TwoAudio.createTwoAudioArrays(soundsList);
        adapter = new CustomAdapter(this, twoSoundsList);
        ListView soundsListView = findViewById(R.id.listview_1);
        soundsListView.setAdapter(adapter);

        // Init du picker
        pickAudioLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            // Conserver l’accès au-delà du redémarrage de l’app
                            final int flags = result.getData().getFlags()
                                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                            getContentResolver().takePersistableUriPermission(uri, flags & Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            // Nom “humain” basé sur le fichier
                            String title = queryDisplayName(uri);
                            if (title == null || title.trim().isEmpty()) title = "nouveau_son";

                            // Ajouter à la source de données
                            soundsList.add(new Audio(title, uri.toString()));
                            // Recréer la liste 2-par-2 et rafraîchir l’adapter
                            ArrayList<TwoAudio> refreshed = TwoAudio.createTwoAudioArrays(soundsList);
                            adapter.updateData(refreshed);   // -> ajoute une méthode dans l’adapter (voir plus bas)

                            // (Optionnel) Persister l’URI et le titre
                            persistSound(title, uri);
                        }
                    }
                }
        );

        // Bouton +
        findViewById(R.id.btn_add_sound).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*"); // mp3/ogg/wav…
            pickAudioLauncher.launch(intent);
        });
    }
    private void persistSound(String title, Uri uri) {
        SharedPreferences sp = getSharedPreferences("sounds", MODE_PRIVATE);
        sp.edit().putString(title, uri.toString()).apply();
    }

    private String queryDisplayName(Uri uri) {
        Cursor c = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
        try {
            if (c != null && c.moveToFirst()) return c.getString(0);
        } finally {
            if (c != null) c.close();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundPlayer sp = SoundPlayer.get(this);
        sp.release();     // Libère le lecteur de sons
    }
}