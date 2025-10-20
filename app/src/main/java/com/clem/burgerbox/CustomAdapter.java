package com.clem.burgerbox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

// CustomAdapter est l’adaptateur qui connecte la donnée (ArrayList<TwoAudio>) à la vue (ListView).
public class CustomAdapter extends BaseAdapter {
    private final ArrayList<TwoAudio> listData;
    private final LayoutInflater layoutInflater;
    private final Context context;
    private final OnAudioAction action;

    public CustomAdapter(Context context, ArrayList<TwoAudio> listData, OnAudioAction action) {
        this.listData = listData;
        this.layoutInflater = LayoutInflater.from(context);
        this.context = context;
        this.action = action;
    }

    // Méthode de base
    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.two_buttons, null);
            holder = new ViewHolder();
            holder.button1 = convertView.findViewById(R.id.bouton1);
            holder.button2 = convertView.findViewById(R.id.bouton2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        TwoAudio item = listData.get(position);

        // Bouton 1
        holder.button1.setText(item.getAudio1().getTitle());
        holder.button1.setOnClickListener(v -> play(item.getAudio1()));

        // appui long pour supprimer si c'est un import
        holder.button1.setOnLongClickListener(v -> {
            Audio a = item.getAudio1();
            if (a != null && a.isFromUri()) {
                new androidx.appcompat.app.AlertDialog.Builder(context)
                        .setTitle("Supprimer ce son ?")
                        .setMessage(a.getTitle())
                        .setPositiveButton("Supprimer", (d, w) -> action.onDelete(a))
                        .setNegativeButton("Annuler", null)
                        .show();
                return true;
            }
            return false; // pas d'action si son par défaut
        });

        // Bouton 2 (peut être null)
        if (item.getAudio2() == null) {
            holder.button2.setVisibility(View.GONE);
            holder.button2.setOnClickListener(null);
        } else {
            holder.button2.setVisibility(View.VISIBLE);
            holder.button2.setText(item.getAudio2().getTitle());
            holder.button2.setOnClickListener(v -> play(item.getAudio2()));

            holder.button2.setOnLongClickListener(v -> {
                Audio a = item.getAudio2();
                if (a != null && a.isFromUri()) {
                    new androidx.appcompat.app.AlertDialog.Builder(context)
                            .setTitle("Supprimer ce son ?")
                            .setMessage(a.getTitle())
                            .setPositiveButton("Supprimer", (d, w) -> action.onDelete(a))
                            .setNegativeButton("Annuler", null)
                            .show();
                    return true;
                }
                return false;
            });
        }
        return convertView;
    }

    // Lance la lecture du son
    private void playByTitle(String title) {
        // check le volume avant de lancer le son
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int vol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (vol == 0) {
            Toast.makeText(context, "Volume média à 0 🔇", Toast.LENGTH_SHORT).show();
            return;
        }
        int resId = resolveRawResId(context, title);
        if (resId != 0) {
            SoundPlayer.get(context).play(resId);
        } else {
            Toast.makeText(context, "Son introuvable: " + title, Toast.LENGTH_SHORT).show();
        }
    }

    private void play(Audio audio) {
        AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null && am.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            Toast.makeText(context, "Volume média à 0 🔇", Toast.LENGTH_SHORT).show();
        }

        if (audio.isFromUri()) {
            Uri uri = Uri.parse(audio.getUri());
            SoundPlayer.get(context).play(uri);
        } else {
            playByTitle(audio.getTitle());
        }
    }


    // Correspondance entre l'id et le nom
    @SuppressLint("DiscouragedApi")
    private int resolveRawResId(Context ctx, String displayName) {
        // "Nino1" -> "nino1"
        String key = displayName == null ? "" : displayName.trim().toLowerCase();
        return ctx.getResources().getIdentifier(key, "raw", ctx.getPackageName());
    }


    // Mémorise les références des sous-vues
    static class ViewHolder {
        Button button1;
        Button button2;
    }
    public void updateData(ArrayList<TwoAudio> newData) {
        this.listData.clear();
        this.listData.addAll(newData);
        notifyDataSetChanged();
    }

    public interface OnAudioAction {
        void onDelete(Audio audio);
    }
}
