package com.liguang.datafetcher;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements DataFetcher.Callback {
    private DataFetcher mDataFetcher;
    private List<String> mData;
    private String mUrl = "https://github.com/passionli";
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.recyclerView)
    RecyclerView mRecyclerView;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MyAdapter();
        mRecyclerView.setAdapter(mAdapter);

        mProgressBar.setVisibility(View.VISIBLE);
        mDataFetcher = new DataFetcher(mUrl, this);
        mDataFetcher.execute();
    }

    @Override
    public void onNext(List<String> data) {
        if (mData == null) {
            mData = new ArrayList<>();
        }
        //merge
        for (String element : data) {
            if (!mData.contains(element)) {
                mData.add(element);
            }
        }
        //sort
        Collections.sort(mData);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onComplete() {
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregister to low level
        mDataFetcher.cancel();
    }


    class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        LayoutInflater mInflater;

        public MyAdapter() {
            mInflater = LayoutInflater.from(MainActivity.this);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(mInflater.inflate(R.layout.list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            ((MyViewHolder) holder).tv.setText(mData.get(position));
        }

        @Override
        public int getItemCount() {
            if (mData == null) {
                return 0;
            } else {
                return mData.size();
            }
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv)
        TextView tv;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
