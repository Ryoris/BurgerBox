package com.clem.burgerbox;

import android.os.Bundle;

public class Lore_du_burger extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setActivityLayout(R.layout.lore_du_burger);

        if (myToolbar != null) {
            myToolbar.setTitle("Le Lore du Burger Sacré de la Space");
        }
    }
}