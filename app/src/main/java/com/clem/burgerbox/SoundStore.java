package com.clem.burgerbox;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class SoundStore {
    private static final String PREFS = "sounds";       //nom du fichier (sons)
    private static final String KEY_LIST = "list";  //nom de la clé du fichier


    // Pour chaque objet Audio, on crée un objet JSON et on l'ajoute au tableau JSON
    public static void save(Context ctx, ArrayList<Audio> sounds) {
        try {
            JSONArray arr = new JSONArray();
            for (Audio a : sounds) {
                JSONObject o = new JSONObject();
                o.put("title", a.getTitle());
                o.put("uri", a.getUri()); // peut être null
                arr.put(o);
            }

            // Ouvre le fichier sounds.xml et sauvegarde les données dedans
            SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
            sp.edit().putString(KEY_LIST, arr.toString()).apply();
        } catch (Exception ignored) {}
    }

    // Récupère le fichier sounds.xml et extrait la liste de sons enregistés
    public static ArrayList<Audio> load(Context ctx) {
        ArrayList<Audio> res = new ArrayList<>();
        SharedPreferences sp = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String json = sp.getString(KEY_LIST, null);
        if (json == null) return res;

        // Lecture du JSON
        try {
            JSONArray arr = new JSONArray(json);
            for (int i = 0; i < arr.length(); i++) {
                JSONObject o = arr.getJSONObject(i);
                String title = o.optString("title", "son");
                String uri = o.isNull("uri") ? null : o.optString("uri", null);
                res.add(new Audio(title, uri));
            }
        } catch (Exception ignored) {}
        return res;
    }
}
