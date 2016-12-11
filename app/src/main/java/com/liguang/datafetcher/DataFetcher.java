package com.liguang.datafetcher;

import android.os.AsyncTask;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 并行加载本地和远程数据
 */
public class DataFetcher {
    private static final String TAG = "DataFetcher";
    /**
     * 上层，只在主线程读写
     */
    private WeakReference<Callback> mRef;
    private Worker mLocalFetcher;
    private Worker mRemoteFetcher;
    private String mUrl;

    public DataFetcher(String url, Callback callback) {
        mRef = new WeakReference<>(callback);
        mUrl = url;
        //假数据
        mLocalFetcher = new Worker(500, new String[]{"D", "E", "F"});
        mRemoteFetcher = new Worker(2500, new String[]{"A", "B", "C", "D", "E", "F"});
    }

    /**
     * 页面退出时需要调用该方法
     */
    public void cancel() {
        Log.d(TAG, "cancel: ");
        mRef.clear();
        mLocalFetcher.cancel(false);
        mRemoteFetcher.cancel(false);
    }

    public void execute() {
        Log.d(TAG, "execute: ");
        //To avoid AsyncTask's version problems, we schedule our job to parallel executor
        mLocalFetcher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mUrl);
        mRemoteFetcher.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, mUrl);
    }

    private class Worker extends AsyncTask<String, Integer, List<String>> {
        private static final String TAG = "Worker";
        private boolean mCompleted;

        private long mDelay;
        private String[] mMockData;

        public Worker(long delay, String[] mockData) {
            mDelay = delay;
            mMockData = mockData;
        }

        @Override
        protected void onPreExecute() {
            mCompleted = false;
        }

        @Override
        protected List<String> doInBackground(String... strings) {
            Log.d(TAG, "doInBackground: enter worker " + System.identityHashCode(this));
            try {
                //模拟耗时
                Thread.sleep(mDelay);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "doInBackground: exit worker " + System.identityHashCode(this));
            return new ArrayList<>(Arrays.asList(mMockData));
        }

        @Override
        protected void onPostExecute(List<String> data) {
            mCompleted = true;
            Callback callback = mRef.get();
            if (callback == null) {
                Log.d(TAG, "onPostExecute: worker complete but high level callback is null");
                return;
            }
            callback.onNext(data);
            checkState();
        }

        @Override
        protected void onCancelled() {
            Log.d(TAG, "onCancelled: " + System.identityHashCode(this));
        }

        public boolean isCompleted() {
            return mCompleted;
        }
    }

    private void checkState() {
        if (mLocalFetcher.isCompleted() && mRemoteFetcher.isCompleted()) {
            //这里不需要检查Callback是否为空
            // GC线程不回收Callback对象, 其在前一个栈帧(onPostExecute)上引用着
            mRef.get().onComplete();
        }
    }

    interface Callback {
        void onNext(List<String> data);

        void onComplete();
    }
}
