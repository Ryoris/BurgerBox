package com.clem.burgerbox;

import android.os.Bundle;
import android.widget.ListView;
import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityLayout(R.layout.activity_main);

        ListView soundsListView = findViewById(R.id.listview_1);
        ArrayList<Audio> soundsList = new ArrayList<>();
        ArrayList<TwoAudio> twoSoundsList;

        // Liste de sons par défaut
        soundsList.add(new Audio("big_bell"));
        soundsList.add(new Audio("fart1"));
        soundsList.add(new Audio("throwing"));

        twoSoundsList=TwoAudio.createTwoAudioArrays(soundsList);
        CustomAdapter adapter = new CustomAdapter(this, twoSoundsList);
        soundsListView.setAdapter(adapter);

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        SoundPlayer sp = SoundPlayer.get(this);
        sp.release();     // Libère le lecteur de sons
    }
}