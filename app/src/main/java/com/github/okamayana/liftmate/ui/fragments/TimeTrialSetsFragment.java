package com.github.okamayana.liftmate.ui.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.google.DividerItemDecoration;
import com.github.okamayana.liftmate.ui.adapters.SetAdapter;
import com.github.okamayana.liftmate.ui.adapters.SetAdapter.Set;

public class TimeTrialSetsFragment extends Fragment {

    public static final String ACTION_SET_COMPLETE = "com.github.okamayana.liftmate.ACTION_SET_COMPLETE";
    public static final String ACTION_RESET = "com.github.okamayana.liftmate.ACTION_RESET";

    public static final String EXTRA_SET_NUNBER = "extra_set_number";
    public static final String EXTRA_SET_TIME = "extra_set_time";
    public static final String EXTRA_REPS_IN_SET = "extra_reps_in_set";

    public static TimeTrialSetsFragment newInstance() {
        return new TimeTrialSetsFragment();
    }

    private SetBroadcastReceiver mSetBroadcastReceiver;

    private RecyclerView mSetListView;
    private View mEmptyView;
    private SetAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSetBroadcastReceiver = new SetBroadcastReceiver();
        getActivity().registerReceiver(mSetBroadcastReceiver, new IntentFilter(ACTION_SET_COMPLETE));
        getActivity().registerReceiver(mSetBroadcastReceiver, new IntentFilter(ACTION_RESET));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time_trial_sets, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mAdapter = new SetAdapter(getActivity());

        mSetListView = (RecyclerView) view.findViewById(R.id.sets_list_view);
        mSetListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mSetListView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL_LIST));
        mSetListView.setHasFixedSize(true);
        mSetListView.setAdapter(mAdapter);

        mEmptyView = view.findViewById(R.id.sets_empty);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mSetBroadcastReceiver);
    }

    private void handleReset() {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();

        mEmptyView.setVisibility(View.VISIBLE);
        mSetListView.setVisibility(View.GONE);
    }

    private void handleSetComplete(Intent intent) {
        int setNumber = intent.getIntExtra(EXTRA_SET_NUNBER, 0);
        long setTime = intent.getLongExtra(EXTRA_SET_TIME, 0);
        int repsInSet = intent.getIntExtra(EXTRA_REPS_IN_SET, 0);
        long repTime = (long) Math.ceil(setTime / repsInSet);

        Set set = new Set(setNumber, setTime, repTime);
        mAdapter.add(set);
        mAdapter.notifyDataSetChanged();

        mEmptyView.setVisibility(View.GONE);
        mSetListView.setVisibility(View.VISIBLE);
    }

    private class SetBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_SET_COMPLETE.equals(action)) {
                handleSetComplete(intent);
            } else if (ACTION_RESET.equals(action)) {
                handleReset();
            }
        }
    }
}
