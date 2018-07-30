package com.dokitari.tekomaps.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dokitari.tekomaps.R;
import com.dokitari.tekomaps.models.AlertLocation;

import java.util.List;

public class AlertAdapter extends RecyclerView.Adapter<AlertAdapter.ViewHolder> {
    private List<AlertLocation> mAlertLocations;
    private Context mContext;
    private final AlertAdapter.OnclickHandler mClickHandler;

    public interface OnclickHandler {
        void onAlertClicked(List<AlertLocation> list, int adapterPosition);
    }

    public AlertAdapter(Context context, List<AlertLocation> locations, AlertAdapter.OnclickHandler listAdapterOnclickHandler) {
        mContext = context;
        mAlertLocations = locations;
        mClickHandler = listAdapterOnclickHandler;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_alert, null, false));
    }

    public List<AlertLocation> getLocations() {
        return mAlertLocations;
    }

    public void setQuestions(List<AlertLocation> locations) {
        this.mAlertLocations = locations;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mAlertLocations != null) {
            AlertLocation alertLocation = mAlertLocations.get(position);
            holder.mAddressTextView.setText(alertLocation.getAddress());
        }
    }

    @Override
    public int getItemCount() {
        if (null == mAlertLocations) {
            return 0;
        }
        return mAlertLocations.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView mAddressTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            mAddressTextView = itemView.findViewById(R.id.address);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            mClickHandler.onAlertClicked(mAlertLocations, adapterPosition);
        }
    }

}
