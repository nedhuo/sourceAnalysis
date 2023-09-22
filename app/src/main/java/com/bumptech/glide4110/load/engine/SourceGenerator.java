package com.bumptech.glide4110.load.engine;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide4110.load.DataSource;
import com.bumptech.glide4110.load.Encoder;
import com.bumptech.glide4110.load.Key;
import com.bumptech.glide4110.load.data.DataFetcher;
import com.bumptech.glide4110.load.model.ModelLoader;
import com.bumptech.glide4110.util.LogTime;
import com.bumptech.glide4110.util.Synthetic;

import java.util.Collections;

/**
 * Generates {@link com.bumptech.glide4110.load.data.DataFetcher DataFetchers} from original source data
 * using registered {@link com.bumptech.glide4110.load.model.ModelLoader ModelLoaders} and the model
 * provided for the load.
 *
 * <p>Depending on the disk cache strategy, source data may first be written to disk and then loaded
 * from the cache file rather than returned directly.
 */
class SourceGenerator implements DataFetcherGenerator, DataFetcherGenerator.FetcherReadyCallback {
    private static final String TAG = "SourceGenerator";

    private final DecodeHelper<?> helper;
    private final FetcherReadyCallback cb;

    private int loadDataListIndex;
    private DataCacheGenerator sourceCacheGenerator;
    private Object dataToCache;
    private volatile ModelLoader.LoadData<?> loadData;
    private DataCacheKey originalKey;

    SourceGenerator(DecodeHelper<?> helper, FetcherReadyCallback cb) {
        this.helper = helper;
        this.cb = cb;
    }

    /**
     * @return
     */
    @Override
    public boolean startNext() {
        if (dataToCache != null) {
            Object data = dataToCache;
            dataToCache = null;
            cacheData(data);
        }

        if (sourceCacheGenerator != null && sourceCacheGenerator.startNext()) {
            return true;
        }
        sourceCacheGenerator = null;

        loadData = null;
        boolean started = false;
        while (!started && hasNextModelLoader()) {
            //1. 此处拿String类型链接举例 loadData为 LoadData<>(url, new HttpUrlFetcher(url, timeout))
            loadData = helper.getLoadData().get(loadDataListIndex++);
            //2. 则根据1 此处loadData.fetcher为HttpUrlFetcher dataSource DataSource.REMOTE
            if (loadData != null
                    && (helper.getDiskCacheStrategy().isDataCacheable(loadData.fetcher.getDataSource())
                    || helper.hasLoadPath(loadData.fetcher.getDataClass()))) {
                started = true;
                startNextLoad(loadData);
            }
        }
        return started;
    }

    /**
     * 加载数据的地方
     *
     * @param toStart
     */
    private void startNextLoad(final ModelLoader.LoadData<?> toStart) {
        loadData.fetcher.loadData(
                helper.getPriority(),
                new DataFetcher.DataCallback<Object>() {
                    @Override
                    public void onDataReady(@Nullable Object data) {
                        if (isCurrentRequest(toStart)) {
                            onDataReadyInternal(toStart, data);
                        }
                    }

                    @Override
                    public void onLoadFailed(@NonNull Exception e) {
                        if (isCurrentRequest(toStart)) {
                            onLoadFailedInternal(toStart, e);
                        }
                    }
                });
    }

    // We want reference equality explicitly to make sure we ignore results from old requests.
    @SuppressWarnings({"PMD.CompareObjectsWithEquals", "WeakerAccess"})
    @Synthetic
    boolean isCurrentRequest(ModelLoader.LoadData<?> requestLoadData) {
        ModelLoader.LoadData<?> currentLoadData = loadData;
        return currentLoadData != null && currentLoadData == requestLoadData;
    }

    /**
     * loadDataListIndex 是集合索引
     *
     * @return
     */
    private boolean hasNextModelLoader() {
        return loadDataListIndex < helper.getLoadData().size();
    }

    /**
     * 缓存数据
     *
     * @param dataToCache
     */
    private void cacheData(Object dataToCache) {
        long startTime = LogTime.getLogTime();
        try {
            Encoder<Object> encoder = helper.getSourceEncoder(dataToCache);
            DataCacheWriter<Object> writer =
                    new DataCacheWriter<>(encoder, dataToCache, helper.getOptions());
            originalKey = new DataCacheKey(loadData.sourceKey, helper.getSignature());
            //将数据缓存到磁盘缓存
            helper.getDiskCache().put(originalKey, writer);
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(
                        TAG,
                        "Finished encoding source to cache"
                                + ", key: "
                                + originalKey
                                + ", data: "
                                + dataToCache
                                + ", encoder: "
                                + encoder
                                + ", duration: "
                                + LogTime.getElapsedMillis(startTime));
            }
        } finally {
            loadData.fetcher.cleanup();
        }

        sourceCacheGenerator =
                new DataCacheGenerator(Collections.singletonList(loadData.sourceKey), helper, this);
    }

    @Override
    public void cancel() {
        ModelLoader.LoadData<?> local = loadData;
        if (local != null) {
            local.fetcher.cancel();
        }
    }

    @SuppressWarnings("WeakerAccess")
    @Synthetic
    void onDataReadyInternal(ModelLoader.LoadData<?> loadData, Object data) {
        DiskCacheStrategy diskCacheStrategy = helper.getDiskCacheStrategy();
        if (data != null && diskCacheStrategy.isDataCacheable(loadData.fetcher.getDataSource())) {
            dataToCache = data;
            // We might be being called back on someone else's thread. Before doing anything, we should
            // reschedule to get back onto Glide's thread.
            cb.reschedule();
        } else {
            cb.onDataFetcherReady(
                    loadData.sourceKey,
                    data,
                    loadData.fetcher,
                    loadData.fetcher.getDataSource(),
                    originalKey);
        }
    }

    @SuppressWarnings("WeakerAccess")
    @Synthetic
    void onLoadFailedInternal(ModelLoader.LoadData<?> loadData, @NonNull Exception e) {
        cb.onDataFetcherFailed(originalKey, e, loadData.fetcher, loadData.fetcher.getDataSource());
    }

    @Override
    public void reschedule() {
        // We don't expect this to happen, although if we ever need it to we can delegate to our
        // callback.
        throw new UnsupportedOperationException();
    }

    // Called from source cache generator.
    @Override
    public void onDataFetcherReady(
            Key sourceKey, Object data, DataFetcher<?> fetcher, DataSource dataSource, Key attemptedKey) {
        // This data fetcher will be loading from a File and provide the wrong data source, so override
        // with the data source of the original fetcher
        cb.onDataFetcherReady(sourceKey, data, fetcher, loadData.fetcher.getDataSource(), sourceKey);
    }

    @Override
    public void onDataFetcherFailed(
            Key sourceKey, Exception e, DataFetcher<?> fetcher, DataSource dataSource) {
        cb.onDataFetcherFailed(sourceKey, e, fetcher, loadData.fetcher.getDataSource());
    }
}
