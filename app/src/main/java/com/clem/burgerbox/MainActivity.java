package com.clem.burgerbox;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.clem.burgerbox.R;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    // un super commentaire
    //et un deuxieme

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityLayout(R.layout.activity_main);

        ListView soundsListView = (ListView) findViewById(R.id.listview_1);

        ArrayList<Audio> soundsList = new ArrayList<>();

        ArrayList<TwoAudio> twoSoundsList = new ArrayList<>();

        soundsList.add(new Audio("Nino1"));
        soundsList.add(new Audio("Nino2"));
        soundsList.add(new Audio("Nino3"));
        soundsList.add(new Audio("Nino4"));
        soundsList.add(new Audio("Nino5"));
        soundsList.add(new Audio("Nino6"));
        soundsList.add(new Audio("Nino7"));
        soundsList.add(new Audio("Nino8"));
        soundsList.add(new Audio("Nino9"));

        twoSoundsList=TwoAudio.createTwoAudioArrays(soundsList);


        CustomAdapter adapter = new CustomAdapter(this, twoSoundsList);

        //get your listView and use your adapter
        soundsListView.setAdapter(adapter);

    }
}