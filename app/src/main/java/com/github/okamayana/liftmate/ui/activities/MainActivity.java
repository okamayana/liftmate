package com.github.okamayana.liftmate.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.ui.adapters.MainMenuAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        MainMenuAdapter adapter = new MainMenuAdapter(MainActivity.this);

        RecyclerView menuListView = (RecyclerView) findViewById(R.id.menu_list_view);
        menuListView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        menuListView.setAdapter(adapter);
    }
}
