package com.github.okamayana.liftmate.ui.adapters;

import android.bluetooth.BluetoothDevice;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.github.okamayana.liftmate.ui.fragments.TimeTrialSetsFragment;
import com.github.okamayana.liftmate.ui.fragments.TimeTrialWorkoutFragment;

import java.util.ArrayList;
import java.util.List;

public class TimeTrialPagerAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> mFragments;

    public TimeTrialPagerAdapter(FragmentManager fm, int targetSets, int targetReps,
                                 int targetMinsPerSet, int targetSecsPerSet,
                                 BluetoothDevice device) {
        super(fm);

        mFragments = new ArrayList<>();
        mFragments.add(TimeTrialWorkoutFragment.newInstance(targetSets, targetReps,
                targetMinsPerSet, targetSecsPerSet, device));
        mFragments.add(TimeTrialSetsFragment.newInstance());
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
