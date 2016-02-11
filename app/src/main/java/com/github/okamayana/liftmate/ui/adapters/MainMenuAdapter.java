package com.github.okamayana.liftmate.ui.adapters;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.okamayana.liftmate.R;
import com.github.okamayana.liftmate.ui.activities.FreeStyleActivity;
import com.github.okamayana.liftmate.ui.adapters.MainMenuAdapter.MainMenuViewHolder;
import com.github.okamayana.liftmate.ui.fragments.TimeTrialSetupDialogFragment;

import java.util.Arrays;
import java.util.List;

public class MainMenuAdapter extends Adapter<MainMenuViewHolder> {

    public enum MainMenuItem {
        TIME_TRIAL_MODE(R.string.main_menu_time_trial),
        FREE_STYLE_MODE(R.string.main_menu_free_style);

        public int stringResId;

        MainMenuItem(int stringResId) {
            this.stringResId = stringResId;
        }
    }

    public static class MainMenuViewHolder extends ViewHolder {

        TextView menuTextView;

        public MainMenuViewHolder(View itemView) {
            super(itemView);

            menuTextView = (TextView) itemView.findViewById(R.id.menu_text);
        }
    }

    private Context mContext;
    private List<MainMenuItem> mItems;

    public MainMenuAdapter(Context context) {
        mContext = context;
        mItems = Arrays.asList(MainMenuItem.values());
    }

    @Override
    public MainMenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        MainMenuItem[] items = MainMenuItem.values();
        OnClickListener listener = null;

        switch (items[viewType]) {
            case TIME_TRIAL_MODE:
                listener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showTimeTrialSetupDialog();
                    }
                };
                break;

            case FREE_STYLE_MODE:
                listener = new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FreeStyleActivity.start(mContext);
                    }
                };
                break;
        }

        View view = LayoutInflater.from(mContext).inflate(
                R.layout.list_item_main_menu, parent, false);
        view.setOnClickListener(listener);

        return new MainMenuViewHolder(view);
    }

    private void showTimeTrialSetupDialog() {
        TimeTrialSetupDialogFragment setupDialog =
                TimeTrialSetupDialogFragment.newInstance();
        setupDialog.show(((AppCompatActivity) mContext).getSupportFragmentManager(),
                null);
    }

    @Override
    public void onBindViewHolder(MainMenuViewHolder viewHolder, int position) {
        MainMenuItem menuItem = mItems.get(position);
        viewHolder.menuTextView.setText(menuItem.stringResId);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
