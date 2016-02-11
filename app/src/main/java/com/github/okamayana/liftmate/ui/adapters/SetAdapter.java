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

import java.util.ArrayList;
import java.util.List;

public class SetAdapter extends Adapter<SetViewHolder> {

    public static class Set {

        private int mSetNumber;
        private float mSetTime;
        private float mRepTime;

        public Set(int setNumber, float setTime, float repTime) {
            mSetNumber = setNumber;
            mSetTime = setTime;
            mRepTime = repTime;
        }

        public int getSetNumber() {
            return mSetNumber;
        }

        public float getSetTime() {
            return mSetTime;
        }

        public float getRepTime() {
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
        mItems.add(new Set(1, 30.50f, 3.81f));
        mItems.add(new Set(2, 31.30f, 3.91f));
        mItems.add(new Set(3, 29.60f, 3.70f));

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

        viewHolder.setNumberView.setText(String.valueOf(item.getSetNumber()));
        viewHolder.setTimeView.setText(String.valueOf(item.getSetTime()));
        viewHolder.repTimeView.setText(String.valueOf(item.getRepTime()));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
