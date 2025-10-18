package com.clem.burgerbox;

import android.annotation.SuppressLint;
import android.content.Context;
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

    public CustomAdapter(Context context, ArrayList<TwoAudio> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
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
        holder.button1.setOnClickListener(v -> playByTitle(item.getAudio1().getTitle()));

        // Bouton 2 (peut être null)
        if (item.getAudio2() == null) {
            holder.button2.setVisibility(View.GONE);
            holder.button2.setOnClickListener(null);
        } else {
            holder.button2.setVisibility(View.VISIBLE);
            holder.button2.setText(item.getAudio2().getTitle());
            holder.button2.setOnClickListener(v -> playByTitle(item.getAudio2().getTitle()));
        }
        return convertView;
    }

    // Lance la lecture du son
    private void playByTitle(String title) {
        int resId = resolveRawResId(context, title);
        if (resId != 0) {
            SoundPlayer.get(context).play(resId);
        } else {
            Toast.makeText(context, "Son introuvable: " + title, Toast.LENGTH_SHORT).show();
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
}
