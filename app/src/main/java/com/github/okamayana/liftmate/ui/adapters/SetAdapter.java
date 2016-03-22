package com.github.okamayana.liftmate.ui.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.ui.adapters.SetAdapter.SetViewHolder;
import com.github.okamayana.liftmate.util.DateTimeUtil;

import java.util.ArrayList;
import java.util.List;

public class SetAdapter extends Adapter<SetViewHolder> {

    public static final String FORMAT_TIME = "%02d:%02d";

    public static class Set {

        private int mSetNumber;
        private long mSetTime;
        private long mRepTime;

        public Set(int setNumber, long setTime, long repTime) {
            mSetNumber = setNumber;
            mSetTime = setTime;
            mRepTime = repTime;
        }

        public int getSetNumber() {
            return mSetNumber;
        }

        public long getSetTime() {
            return mSetTime;
        }

        public long getRepTime() {
            return mRepTime;
        }
    }

    public static class SetViewHolder extends ViewHolder {

        TextView setNumberView;
        TextView setTimeView;
        TextView repTimeView;

        public SetViewHolder(View itemView) {
            super(itemView);

            setNumberView = (TextView) itemView.findViewById(R.id.set_number);
            setTimeView = (TextView) itemView.findViewById(R.id.set_time);
            repTimeView = (TextView) itemView.findViewById(R.id.rep_time);
        }
    }

    private List<Set> mItems;
    private Context mContext;

    public SetAdapter(Context context) {
        mItems = new ArrayList<>();
        mContext = context;
    }

    @Override
    public SetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_set, parent, false);
        return new SetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SetViewHolder viewHolder, int position) {
        Set item = mItems.get(position);

        int setNumber = item.getSetNumber();
        viewHolder.setNumberView.setText(String.valueOf(setNumber));

        long setTime = item.getSetTime();
        String setTimestamp = DateTimeUtil.getTimestampFromMillis(setTime, FORMAT_TIME);
        viewHolder.setTimeView.setText(setTimestamp);

        long repTime = item.getRepTime();
        String repTimestamp = DateTimeUtil.getTimestampFromMillis(repTime, FORMAT_TIME);
        viewHolder.repTimeView.setText(repTimestamp);
    }

    public void add(Set item) {
        mItems.add(item);
    }

    public void clear() {
        mItems.clear();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
