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

public class MainActivity extends BaseActivity {        // Hérite de la toolbar
    private ActivityResultLauncher<Intent> pickAudioLauncher;       // picker de documents audio
    private ArrayList<Audio> soundsList;        // Liste source de tous les sons visibles
    private CustomAdapter adapter;      // Adapter pour connecter les données (TwoAudio) au ListView.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityLayout(R.layout.activity_main);      // superpose activity_main.xml sur activity_base.xml

        // 1) Construire la liste: défauts + persistés
        soundsList = loadDefaultSounds();
        soundsList.addAll(SoundStore.load(this));


        // 2) Convertit la liste de Audio en éléments TwoAudio (chaque item regroupe 2 sons pour un affichage en “grille 2-par-2” dans un ListView)
        ArrayList<TwoAudio> twoSoundsList = TwoAudio.createTwoAudioArrays(soundsList);
        adapter = new CustomAdapter(
                this,
                twoSoundsList,
                audio -> { // <- callback (supprimer le son)
                    if (audio != null && audio.isFromUri()) {   // supprime que les sons ajoutés par l’utilisateur
                        String key = audio.getUri();
                        for (int i = soundsList.size() - 1; i >= 0; i--) {
                            Audio a = soundsList.get(i);
                            if (a.isFromUri() && key.equals(a.getUri())) {
                                soundsList.remove(i);
                            }
                        }
                        SoundStore.save(this, onlyUserSounds(soundsList));            // Sauvegarder la nouvelle liste utilisateur
                        ArrayList<TwoAudio> refreshed = TwoAudio.createTwoAudioArrays(soundsList);      // Rafraîchir la liste
                        adapter.updateData(refreshed);
                        Toast.makeText(this, "Son supprimé ✅", Toast.LENGTH_SHORT).show();      //  Message toast
                    }
                }
        );

        // 3) Connecte le ListView à l'adapteur
        ListView soundsListView = findViewById(R.id.listview_1);
        soundsListView.setAdapter(adapter);


        // 4) Picker pour ajouter un son
        pickAudioLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        if (uri != null) {

                            // IMPORTANT : Conserver l’accès à l’URI au-delà des redémarrages
                            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

                            String title = queryDisplayName(uri);       // attribution du nom basé sur le fichier
                            if (title == null || title.trim().isEmpty()) title = "nouveau_son";

                            soundsList.add(new Audio(title, uri.toString()));   // Ajout à la liste
                            ArrayList<TwoAudio> refreshed = TwoAudio.createTwoAudioArrays(soundsList);  // Rafraichit l'UI
                            adapter.updateData(refreshed);

                            SoundStore.save(this, onlyUserSounds(soundsList));      // Sauvegarder en JSON via SoundStore
                        }
                    }
                }
        );

        // Bouton + (ajout de son)
        findViewById(R.id.btn_add_sound).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);    // Attache un listener
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");       // mp3/ogg/wav…
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);  //permission persistante du stockage au démarrage
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);     //permission lecture
            pickAudioLauncher.launch(intent);       // lance le pickeur définit plus haut
        });
    }

    // Au passage en arrière-plan, re-sauvegarde la liste des sons utilisateur
    @Override protected void onStop() {
        super.onStop();
        SoundStore.save(this, onlyUserSounds(soundsList));
    }

    // Filtrer les sons utilisateur (Uri)
    private ArrayList<Audio> onlyUserSounds(ArrayList<Audio> all) {
        ArrayList<Audio> res = new ArrayList<>();
        for (Audio a : all) if (a.isFromUri()) res.add(a);
        return res;
    }

    // Récupére le nom affichable d’un Uri
    private String queryDisplayName(Uri uri) {
        try (Cursor c = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) return c.getString(0);
        }
        return null;
    }

    // Charger et trie les sons par défaut
    private ArrayList<Audio> loadDefaultSounds() {
        ArrayList<Audio> list = new ArrayList<>();
        try {
            Field[] fields = R.raw.class.getFields();     // Récupère tous les champs de la classe R.raw
            ArrayList<String> names = new ArrayList<>();
            for (Field f : fields) names.add(f.getName());

            java.util.Collections.sort(names, String.CASE_INSENSITIVE_ORDER);   // Tri A-Z
            for (String name : names) {     // Construit les Audio avec l'id de res
                list.add(new Audio(name));
            }
        } catch (Exception e) {
            Log.e("MainActivity", "Erreur lors du chargement des sons par défaut", e);
        }

        return list;
    }


    // Libérer le lecteur de sons
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundPlayer sp = SoundPlayer.get(this);
        sp.release();
    }
}