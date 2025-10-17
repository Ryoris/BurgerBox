package com.clem.burgerbox;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.MenuInflater;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.widget.PopupMenu;

public abstract class BaseActivity extends AppCompatActivity {

    protected Toolbar myToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_base);

        // Gère les insets système pour la zone de contenu
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content_frame), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, v.getPaddingTop(), systemBars.right, systemBars.bottom);
            return insets;
        });

        myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        // Icône et menu "hamburger"
        myToolbar.setNavigationIcon(R.drawable.baseline_reorder_20);
        myToolbar.setNavigationOnClickListener(v -> {

            Context wrapper = new ContextThemeWrapper(BaseActivity.this, R.style.PopupMenuYellow);
            PopupMenu popupMenu = new PopupMenu(wrapper, v);

            MenuInflater inflater = popupMenu.getMenuInflater();
            inflater.inflate(R.menu.action_buttons, popupMenu.getMenu());

            popupMenu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.soundboard) {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                    return true;
                } else if (id == R.id.importer_un_son) {
                    // TODO: implémenter
                    return true;
                } else if (id == R.id.générer_un_son) {
                    startActivity(new Intent(this, Generer_son.class));
                    finish();
                    return true;
                } else if (id == R.id.lore_du_burger) {
                    startActivity(new Intent(this, Lore_du_burger.class));
                    finish();
                    return true;
                }
                return false;
            });
            popupMenu.show();
        });
    }

    /** Injecte le layout spécifique de l’écran dans le FrameLayout du shell. */
    protected void setActivityLayout(@LayoutRes int layoutResID) {
        FrameLayout contentFrame = findViewById(R.id.content_frame);
        contentFrame.removeAllViews();
        getLayoutInflater().inflate(layoutResID, contentFrame, true);
    }
}
