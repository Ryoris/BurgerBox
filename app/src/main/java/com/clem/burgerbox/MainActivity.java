package com.clem.burgerbox;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;


import java.lang.reflect.Field;
import java.util.ArrayList;

public class MainActivity extends BaseActivity {
    private ActivityResultLauncher<Intent> pickAudioLauncher;
    private ArrayList<Audio> soundsList;              // rends-le champ d’instance
    private CustomAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityLayout(R.layout.activity_main);

        // 1) Construire la liste: défauts + persistés
        soundsList = loadDefaultSounds();
        soundsList.addAll(SoundStore.load(this));

        // 2) Adapter
        ArrayList<TwoAudio> twoSoundsList = TwoAudio.createTwoAudioArrays(soundsList);
        adapter = new CustomAdapter(
                this,
                twoSoundsList,
                audio -> { // <- callback OnAudioAction.onDelete
                    // 1) Retirer de la source
                    if (audio != null && audio.isFromUri()) {
                        // supprime l'entrée équivalente par URI
                        String key = audio.getUri();
                        for (int i = soundsList.size() - 1; i >= 0; i--) {
                            Audio a = soundsList.get(i);
                            if (a.isFromUri() && key.equals(a.getUri())) {
                                soundsList.remove(i);
                            }
                        }
                        // 2) Sauvegarder la nouvelle liste utilisateur
                        SoundStore.save(this, onlyUserSounds(soundsList));

                        // 3) Rafraîchir la liste 2-par-2 + adapter
                        ArrayList<TwoAudio> refreshed = TwoAudio.createTwoAudioArrays(soundsList);
                        adapter.updateData(refreshed);

                        // 4) Feedback
                        Toast.makeText(this, "Son supprimé ✅", Toast.LENGTH_SHORT).show();
                    }
                }
        );
        ListView soundsListView = findViewById(R.id.listview_1);
        soundsListView.setAdapter(adapter);

        // 3) Picker pour ajouter un son
        pickAudioLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {
                            // 1) Conserver l’accès à l’URI au-delà des redémarrages
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            // Nom “humain” basé sur le fichier
                            String title = queryDisplayName(uri);
                            if (title == null || title.trim().isEmpty()) title = "nouveau_son";

                            // 2) Ajouter à la liste en mémoire
                            soundsList.add(new Audio(title, uri.toString()));

                            // 3) Rafraîchir l’UI
                            ArrayList<TwoAudio> refreshed = TwoAudio.createTwoAudioArrays(soundsList);
                            adapter.updateData(refreshed);

                            // 4) Sauvegarder en JSON via SoundStore (ne persiste que les sons utilisateur)
                            SoundStore.save(this, onlyUserSounds(soundsList));
                        }
                    }
                }
        );

        // Bouton +
        findViewById(R.id.btn_add_sound).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*"); // mp3/ogg/wav…
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);  //permission stockage au démarrage
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickAudioLauncher.launch(intent);
        });
    }

    @Override protected void onStop() {
        super.onStop();
        SoundStore.save(this, onlyUserSounds(soundsList));
    }

    private ArrayList<Audio> onlyUserSounds(ArrayList<Audio> all) {
        ArrayList<Audio> res = new ArrayList<>();
        for (Audio a : all) if (a.isFromUri()) res.add(a);
        return res;
    }

    private String queryDisplayName(Uri uri) {
        try (Cursor c = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) return c.getString(0);
        }
        return null;
    }


    private ArrayList<Audio> loadDefaultSounds() {
        ArrayList<Audio> list = new ArrayList<>();

        try {
            // Récupère tous les champs de la classe R.raw
            Field[] fields = R.raw.class.getFields();
            for (Field f : fields) {
                String name = f.getName(); // exemple : "big_bell"
                list.add(new Audio(name)); // tu peux garder le nom brut, ou formater pour l'affichage
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Erreur lors du chargement des sons par défaut", e);
        }

        return list;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundPlayer sp = SoundPlayer.get(this);
        sp.release();     // Libère le lecteur de sons
    }
}