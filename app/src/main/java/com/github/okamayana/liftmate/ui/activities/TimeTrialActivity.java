package com.github.okamayana.liftmate.ui.activities;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.design.widget.TabLayout.TabLayoutOnPageChangeListener;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.ui.adapters.TimeTrialPagerAdapter;
import com.github.okamayana.liftmate.ui.fragments.TimeTrialWorkoutFragment;

public class TimeTrialActivity extends AppCompatActivity implements OnTabSelectedListener {

    public static final String EXTRA_BLUETOOTH_DEVICE = "extra_bluetooth_device";
    public static final String EXTRA_TARGET_SETS = "extra_target_sets";
    public static final String EXTRA_TARGET_REPS = "extra_target_reps";
    public static final String EXTRA_TARGET_MINS_PER_SET = "extra_target_mins_per_set";
    public static final String EXTRA_TARGET_SECS_PER_SET = "extra_target_secs_per_set";

    public static void start(Context context, int targetSets, int targetReps, int targetMinsPerSet,
                             int targetSecsPerSet, BluetoothDevice device) {
        Intent intent = new Intent(context, TimeTrialActivity.class);
        intent.putExtra(EXTRA_TARGET_SETS, targetSets);
        intent.putExtra(EXTRA_TARGET_REPS, targetReps);
        intent.putExtra(EXTRA_TARGET_MINS_PER_SET, targetMinsPerSet);
        intent.putExtra(EXTRA_TARGET_SECS_PER_SET, targetSecsPerSet);
        intent.putExtra(EXTRA_BLUETOOTH_DEVICE, device);
        context.startActivity(intent);
    }

    private ViewPager mViewPager;
    private TimeTrialPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_trial);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mViewPager = (ViewPager) findViewById(R.id.time_trial_view_pager);
        mViewPager.setOffscreenPageLimit(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Current"));
        tabLayout.addTab(tabLayout.newTab().setText("Completed"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        Intent intent = getIntent();
        FragmentManager fm = getSupportFragmentManager();
        BluetoothDevice device = intent.getParcelableExtra(EXTRA_BLUETOOTH_DEVICE);

        int targetSets = intent.getIntExtra(EXTRA_TARGET_SETS, 0);
        int targetReps = intent.getIntExtra(EXTRA_TARGET_REPS, 0);
        int targetMinsPerSet = intent.getIntExtra(EXTRA_TARGET_MINS_PER_SET, 0);
        int targetSecsPerSet = intent.getIntExtra(EXTRA_TARGET_SECS_PER_SET, 0);

        mPagerAdapter = new TimeTrialPagerAdapter(fm, targetSets, targetReps,
                targetMinsPerSet, targetSecsPerSet, device);
        mViewPager.setAdapter(mPagerAdapter);

        mViewPager.addOnPageChangeListener(new TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(TimeTrialActivity.this);
        mViewPager.setCurrentItem(0);
    }

    @Override
    public void onBackPressed() {
        OnBackPressedListener listener = (OnBackPressedListener) mPagerAdapter.getItem(0);

        if (listener != null) {
            listener.onBackPressed();
        }
    }

    @Override
    public void onTabSelected(Tab tab) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(Tab tab) {}

    @Override
    public void onTabReselected(Tab tab) {}


    public interface OnBackPressedListener {

        void onBackPressed();
    }
}
