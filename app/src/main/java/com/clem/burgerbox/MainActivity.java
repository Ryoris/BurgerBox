package com.clem.burgerbox;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.clem.burgerbox.R;

public class MainActivity extends AppCompatActivity {

    // un super commentaire
    //et un deuxieme

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setNavigationIcon(R.drawable.baseline_reorder_20);
        myToolbar.setNavigationOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.action_buttons, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.soundboard) {
                    Intent intent = new Intent(this, MainActivity.class);
                    startActivity(intent);
                    return true;
                } else if (id == R.id.importer_un_son) {

                    return true;
                } else if (id == R.id.générer_un_son) {

                    return true;
                } else if (id == R.id.lore_du_burger) {
                    Intent intent = new Intent(this, Lore_du_burger.class);
                    startActivity(intent);
                    finish();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    protected void setActivityLayout(int layoutResID) {
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.removeAllViews();
        getLayoutInflater().inflate(layoutResID, contentFrame, true);
    }
}
