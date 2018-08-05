package com.dokitari.tekomaps.ui.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dokitari.tekomaps.R;
import com.dokitari.tekomaps.adapters.AlertAdapter;
import com.dokitari.tekomaps.models.AlertLocation;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlertActivity extends AppCompatActivity implements AlertAdapter.OnclickHandler{

    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;

    private AlertAdapter mAdapter;
    private LinearLayoutManager mLayoutManager;
    private List<AlertLocation> mForumQuestions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alert);
        ButterKnife.bind(this);

        mAdapter = new AlertAdapter(this, mForumQuestions, this::onAlertClicked);

        setDummyLocations();
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
    }

    @Override
    public void onAlertClicked(List<AlertLocation> list, int adapterPosition) {
        AlertLocation location = list.get(adapterPosition);
        Intent intent =  new Intent(AlertActivity.this, MapActivity.class);
        intent.putExtra("lat", location.getLati());
        intent.putExtra("long", location.getLongi());
        intent.putExtra("address", location.getAddress());
        startActivity(intent);
    }

    public void setDummyLocations(){
        List<AlertLocation> locations = new ArrayList<>();
        locations.add(new AlertLocation(55.8163, 49.0938, "Address 1"));
        locations.add(new AlertLocation(56.8163, 50.0938, "Address 2"));
        locations.add(new AlertLocation(55.8163, 51.0938, "Address 3"));
        locations.add(new AlertLocation(57.8163, 53.0938, "Address 4"));
        mAdapter.setQuestions(locations);
    }
}
