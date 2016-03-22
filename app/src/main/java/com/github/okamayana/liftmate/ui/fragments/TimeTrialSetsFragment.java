package com.github.okamayana.liftmate.ui.fragments;

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

public class TimeTrialSetsFragment extends Fragment {

    public static TimeTrialSetsFragment newInstance() {
        return new TimeTrialSetsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_time_trial_sets, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        SetAdapter adapter = new SetAdapter(getActivity());

        RecyclerView setListView = (RecyclerView) view.findViewById(R.id.sets_list_view);
        setListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        setListView.addItemDecoration(new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL_LIST));
        setListView.setHasFixedSize(true);
        setListView.setAdapter(adapter);
    }
}
