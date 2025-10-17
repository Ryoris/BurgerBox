package com.clem.burgerbox;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    private ArrayList<TwoAudio> listData;
    private LayoutInflater layoutInflater;
    private Context context;

    public CustomAdapter(Context context, ArrayList<TwoAudio> listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
        this.context = context;
    }

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

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;


        if (convertView == null) {

            convertView = layoutInflater.inflate(R.layout.two_buttons, null);
            holder = new ViewHolder();

            holder.button1 = (Button) convertView.findViewById(R.id.bouton1);
            holder.button2 = (Button) convertView.findViewById(R.id.bouton2);


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.button1.setText(listData.get(position).getAudio1().getTitle());

        if(listData.get(position).getAudio2()==null){
            holder.button2.setVisibility(View.GONE);
        }
        else{
            holder.button2.setVisibility(View.VISIBLE);
            holder.button2.setText(listData.get(position).getAudio2().getTitle());
        }



        return convertView;
    }

    static class ViewHolder {
        Button button1;
        Button button2;
    }
}
