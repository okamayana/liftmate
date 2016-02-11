package com.github.okamayana.liftmate.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.google.DividerItemDecoration;
import com.github.okamayana.liftmate.ui.adapters.SetAdapter;

public class TimeTrialActivity extends AppCompatActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, TimeTrialActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_trial);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SetAdapter adapter = new SetAdapter(TimeTrialActivity.this);

        RecyclerView setListView = (RecyclerView) findViewById(R.id.sets_list_view);
        setListView.setLayoutManager(new LinearLayoutManager(TimeTrialActivity.this));
        setListView.addItemDecoration(new DividerItemDecoration(TimeTrialActivity.this,
                DividerItemDecoration.VERTICAL_LIST));
        setListView.setHasFixedSize(true);
        setListView.setAdapter(adapter);
    }
}
