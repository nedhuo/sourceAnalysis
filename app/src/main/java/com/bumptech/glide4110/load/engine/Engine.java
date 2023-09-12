package com.bumptech.glide4110.load.engine;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pools;

import com.bumptech.glide4110.load.DataSource;
import com.bumptech.glide4110.load.Key;
import com.bumptech.glide4110.load.Options;
import com.bumptech.glide4110.load.Transformation;
import com.bumptech.glide4110.load.engine.EngineResource.ResourceListener;
import com.bumptech.glide4110.util.LogTime;
import com.bumptech.glide4110.util.pool.FactoryPools;
import com.bumptech.glide4110.GlideContext;
import com.bumptech.glide4110.Priority;
import com.bumptech.glide4110.load.engine.cache.DiskCache;
import com.bumptech.glide4110.load.engine.cache.DiskCacheAdapter;
import com.bumptech.glide4110.load.engine.cache.MemoryCache;
import com.bumptech.glide4110.load.engine.executor.GlideExecutor;
import com.bumptech.glide4110.request.ResourceCallback;
import com.bumptech.glide4110.util.Executors;
import com.bumptech.glide4110.util.Preconditions;
import com.bumptech.glide4110.util.Synthetic;

import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Responsible for starting loads and managing active and cached resources.
 */
public class Engine implements EngineJobListener, MemoryCache.ResourceRemovedListener, ResourceListener {
    private static final String TAG = "Engine";
    private static final int JOB_POOL_SIZE = 150;
    private static final boolean VERBOSE_IS_LOGGABLE = Log.isLoggable(TAG, Log.VERBOSE);
    private final Jobs jobs;
    private final com.bumptech.glide4110.load.engine.EngineKeyFactory keyFactory;
    private final com.bumptech.glide4110.load.engine.cache.MemoryCache cache;
    private final EngineJobFactory engineJobFactory;
    private final com.bumptech.glide4110.load.engine.ResourceRecycler resourceRecycler;
    private final LazyDiskCacheProvider diskCacheProvider;
    private final DecodeJobFactory decodeJobFactory;
    private final com.bumptech.glide4110.load.engine.ActiveResources activeResources;

    public Engine(
            com.bumptech.glide4110.load.engine.cache.MemoryCache memoryCache,
            com.bumptech.glide4110.load.engine.cache.DiskCache.Factory diskCacheFactory,
            com.bumptech.glide4110.load.engine.executor.GlideExecutor diskCacheExecutor,
            com.bumptech.glide4110.load.engine.executor.GlideExecutor sourceExecutor,
            com.bumptech.glide4110.load.engine.executor.GlideExecutor sourceUnlimitedExecutor,
            com.bumptech.glide4110.load.engine.executor.GlideExecutor animationExecutor,
            boolean isActiveResourceRetentionAllowed) {
        this(
                memoryCache,
                diskCacheFactory,
                diskCacheExecutor,
                sourceExecutor,
                sourceUnlimitedExecutor,
                animationExecutor,
                /*jobs=*/ null,
                /*keyFactory=*/ null,
                /*activeResources=*/ null,
                /*engineJobFactory=*/ null,
                /*decodeJobFactory=*/ null,
                /*resourceRecycler=*/ null,
                isActiveResourceRetentionAllowed);
    }

    @VisibleForTesting
    Engine(
            MemoryCache cache,
            com.bumptech.glide4110.load.engine.cache.DiskCache.Factory diskCacheFactory,
            com.bumptech.glide4110.load.engine.executor.GlideExecutor diskCacheExecutor,
            com.bumptech.glide4110.load.engine.executor.GlideExecutor sourceExecutor,
            com.bumptech.glide4110.load.engine.executor.GlideExecutor sourceUnlimitedExecutor,
            com.bumptech.glide4110.load.engine.executor.GlideExecutor animationExecutor,
            Jobs jobs,
            com.bumptech.glide4110.load.engine.EngineKeyFactory keyFactory,
            com.bumptech.glide4110.load.engine.ActiveResources activeResources,
            EngineJobFactory engineJobFactory,
            DecodeJobFactory decodeJobFactory,
            com.bumptech.glide4110.load.engine.ResourceRecycler resourceRecycler,
            boolean isActiveResourceRetentionAllowed) {
        this.cache = cache;
        this.diskCacheProvider = new LazyDiskCacheProvider(diskCacheFactory);

        if (activeResources == null) {
            activeResources = new com.bumptech.glide4110.load.engine.ActiveResources(isActiveResourceRetentionAllowed);
        }
        this.activeResources = activeResources;
        activeResources.setListener(this);

        if (keyFactory == null) {
            keyFactory = new com.bumptech.glide4110.load.engine.EngineKeyFactory();
        }
        this.keyFactory = keyFactory;

        if (jobs == null) {
            jobs = new Jobs();
        }
        this.jobs = jobs;

        if (engineJobFactory == null) {
            engineJobFactory =
                    new EngineJobFactory(
                            diskCacheExecutor,
                            sourceExecutor,
                            sourceUnlimitedExecutor,
                            animationExecutor,
                            /*engineJobListener=*/ this,
                            /*resourceListener=*/ this);
        }
        this.engineJobFactory = engineJobFactory;

        if (decodeJobFactory == null) {
            decodeJobFactory = new DecodeJobFactory(diskCacheProvider);
        }
        this.decodeJobFactory = decodeJobFactory;

        if (resourceRecycler == null) {
            resourceRecycler = new com.bumptech.glide4110.load.engine.ResourceRecycler();
        }
        this.resourceRecycler = resourceRecycler;

        cache.setResourceRemovedListener(this);
    }

    /**
     * Starts a load for the given arguments.
     *
     * <p>Must be called on the main thread.
     *
     * <p>The flow for any request is as follows:
     *
     * <ul>
     *   <li>Check the current set of actively used resources, return the active resource if present,
     *       and move any newly inactive resources into the memory cache.
     *   <li>Check the memory cache and provide the cached resource if present.
     *   <li>Check the current set of in progress loads and add the cb to the in progress load if one
     *       is present.
     *   <li>Start a new load.
     * </ul>
     *
     * <p>Active resources are those that have been provided to at least one request and have not yet
     * been released. Once all consumers of a resource have released that resource, the resource then
     * goes to cache. If the resource is ever returned to a new consumer from cache, it is re-added to
     * the active resources. If the resource is evicted from the cache, its resources are recycled and
     * re-used if possible and the resource is discarded. There is no strict requirement that
     * consumers release their resources so active resources are held weakly.
     *
     * @param width  The target width in pixels of the desired resource.
     * @param height The target height in pixels of the desired resource.
     * @param cb     The callback that will be called when the load completes.
     */
    public <R> LoadStatus load(
            com.bumptech.glide4110.GlideContext glideContext,
            Object model,
            Key signature,
            int width,
            int height,
            Class<?> resourceClass,
            Class<R> transcodeClass,
            com.bumptech.glide4110.Priority priority,
            com.bumptech.glide4110.load.engine.DiskCacheStrategy diskCacheStrategy,
            Map<Class<?>, Transformation<?>> transformations,
            boolean isTransformationRequired,
            boolean isScaleOnlyOrNoTransform,
            Options options,
            boolean isMemoryCacheable,
            boolean useUnlimitedSourceExecutorPool,
            boolean useAnimationPool,
            boolean onlyRetrieveFromCache,
            com.bumptech.glide4110.request.ResourceCallback cb,
            Executor callbackExecutor) {
        long startTime = VERBOSE_IS_LOGGABLE ? LogTime.getLogTime() : 0;

        //1. 通过签名 宽 高等信息构建一个key（用户从活动缓存 内存缓存查找图片）
        com.bumptech.glide4110.load.engine.EngineKey key =
                keyFactory.buildKey(
                        model,
                        signature,
                        width,
                        height,
                        transformations,
                        resourceClass,
                        transcodeClass,
                        options);

        com.bumptech.glide4110.load.engine.EngineResource<?> memoryResource;
        synchronized (this) {
            //2. 查找内存缓存 活动缓存
            memoryResource = loadFromMemory(key, isMemoryCacheable, startTime);

            //3. 缓存为空 创建新的任务
            if (memoryResource == null) {
                return waitForExistingOrStartNewJob(
                        glideContext,
                        model,
                        signature,
                        width,
                        height,
                        resourceClass,
                        transcodeClass,
                        priority,
                        diskCacheStrategy,
                        transformations,
                        isTransformationRequired,
                        isScaleOnlyOrNoTransform,
                        options,
                        isMemoryCacheable,
                        useUnlimitedSourceExecutorPool,
                        useAnimationPool,
                        onlyRetrieveFromCache,
                        cb,
                        callbackExecutor,
                        key,
                        startTime);
            }
        }

        //缓存中命中对应图片
        // Avoid calling back while holding the engine lock, doing so makes it easier for callers to
        // deadlock.
        cb.onResourceReady(memoryResource, DataSource.MEMORY_CACHE);
        return null;
    }

    private <R> LoadStatus waitForExistingOrStartNewJob(
            com.bumptech.glide4110.GlideContext glideContext,
            Object model,
            Key signature,
            int width,
            int height,
            Class<?> resourceClass,
            Class<R> transcodeClass,
            com.bumptech.glide4110.Priority priority,
            com.bumptech.glide4110.load.engine.DiskCacheStrategy diskCacheStrategy,
            Map<Class<?>, Transformation<?>> transformations,
            boolean isTransformationRequired,
            boolean isScaleOnlyOrNoTransform,
            Options options,
            boolean isMemoryCacheable,
            boolean useUnlimitedSourceExecutorPool,
            boolean useAnimationPool,
            boolean onlyRetrieveFromCache,
            com.bumptech.glide4110.request.ResourceCallback cb,
            Executor callbackExecutor,
            EngineKey key,
            long startTime) {

        //1. 当前key的图片是否正在任务中加载
        EngineJob<?> current = jobs.get(key, onlyRetrieveFromCache);
        if (current != null) {
            current.addCallback(cb, callbackExecutor);
            if (VERBOSE_IS_LOGGABLE) {
                logWithTimeAndKey("Added to existing load", startTime, key);
            }
            return new LoadStatus(cb, current);
        }

        //2. EngineJob是一个用来管理图片加载回调的管理类 内部维护了很多线程池
        EngineJob<R> engineJob =
                engineJobFactory.build(
                        key,
                        isMemoryCacheable,
                        useUnlimitedSourceExecutorPool,
                        useAnimationPool,
                        onlyRetrieveFromCache);

        //3. DecodeJob实现Runnable 是一个加载Job
        DecodeJob<R> decodeJob =
                decodeJobFactory.build(
                        glideContext,
                        model,
                        key,
                        signature,
                        width,
                        height,
                        resourceClass,
                        transcodeClass,
                        priority,
                        diskCacheStrategy,
                        transformations,
                        isTransformationRequired,
                        isScaleOnlyOrNoTransform,
                        onlyRetrieveFromCache,
                        options,
                        engineJob);

        jobs.put(key, engineJob);

        engineJob.addCallback(cb, callbackExecutor);
        //4. 将DecodeJob放入线程池中执行 然后会执行DecodeJob的run方法
        engineJob.start(decodeJob);

        if (VERBOSE_IS_LOGGABLE) {
            logWithTimeAndKey("Started new load", startTime, key);
        }
        return new LoadStatus(cb, engineJob);
    }

    @Nullable
    private EngineResource<?> loadFromMemory(
            EngineKey key, boolean isMemoryCacheable, long startTime) {
        if (!isMemoryCacheable) {
            return null;
        }
        //1. 从活动缓存中查找
        EngineResource<?> active = loadFromActiveResources(key);
        if (active != null) {
            if (VERBOSE_IS_LOGGABLE) {
                logWithTimeAndKey("Loaded resource from active resources", startTime, key);
            }
            return active;
        }
        //2. 从内存缓存中查找
        EngineResource<?> cached = loadFromCache(key);
        if (cached != null) {
            if (VERBOSE_IS_LOGGABLE) {
                logWithTimeAndKey("Loaded resource from cache", startTime, key);
            }
            return cached;
        }

        return null;
    }

    private static void logWithTimeAndKey(String log, long startTime, Key key) {
        Log.v(TAG, log + " in " + LogTime.getElapsedMillis(startTime) + "ms, key: " + key);
    }

    @Nullable
    private EngineResource<?> loadFromActiveResources(Key key) {
        EngineResource<?> active = activeResources.get(key);
        if (active != null) {
            active.acquire();
        }

        return active;
    }

    private EngineResource<?> loadFromCache(Key key) {
        EngineResource<?> cached = getEngineResourceFromCache(key);
        if (cached != null) {
            cached.acquire();
            activeResources.activate(key, cached);
        }
        return cached;
    }

    private EngineResource<?> getEngineResourceFromCache(Key key) {
        Resource<?> cached = cache.remove(key);

        final EngineResource<?> result;
        if (cached == null) {
            result = null;
        } else if (cached instanceof EngineResource) {
            // Save an object allocation if we've cached an EngineResource (the typical case).
            result = (EngineResource<?>) cached;
        } else {
            result =
                    new EngineResource<>(
                            cached, /*isMemoryCacheable=*/ true, /*isRecyclable=*/ true, key, /*listener=*/ this);
        }
        return result;
    }

    public void release(Resource<?> resource) {
        if (resource instanceof EngineResource) {
            ((EngineResource<?>) resource).release();
        } else {
            throw new IllegalArgumentException("Cannot release anything but an EngineResource");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public synchronized void onEngineJobComplete(
            EngineJob<?> engineJob, Key key, EngineResource<?> resource) {
        // A null resource indicates that the load failed, usually due to an exception.
        if (resource != null && resource.isMemoryCacheable()) {
            activeResources.activate(key, resource);
        }

        jobs.removeIfCurrent(key, engineJob);
    }

    @Override
    public synchronized void onEngineJobCancelled(EngineJob<?> engineJob, Key key) {
        jobs.removeIfCurrent(key, engineJob);
    }

    @Override
    public void onResourceRemoved(@NonNull final Resource<?> resource) {
        // Avoid deadlock with RequestManagers when recycling triggers recursive clear() calls.
        // See b/145519760.
        resourceRecycler.recycle(resource, /*forceNextFrame=*/ true);
    }

    @Override
    public void onResourceReleased(Key cacheKey, EngineResource<?> resource) {
        activeResources.deactivate(cacheKey);
        if (resource.isMemoryCacheable()) {
            cache.put(cacheKey, resource);
        } else {
            resourceRecycler.recycle(resource, /*forceNextFrame=*/ false);
        }
    }

    public void clearDiskCache() {
        diskCacheProvider.getDiskCache().clear();
    }

    @VisibleForTesting
    public void shutdown() {
        engineJobFactory.shutdown();
        diskCacheProvider.clearDiskCacheIfCreated();
        activeResources.shutdown();
    }

    /**
     * Allows a request to indicate it no longer is interested in a given load.
     *
     * <p>Non-final for mocking.
     */
    public class LoadStatus {
        private final EngineJob<?> engineJob;
        private final com.bumptech.glide4110.request.ResourceCallback cb;

        LoadStatus(ResourceCallback cb, EngineJob<?> engineJob) {
            this.cb = cb;
            this.engineJob = engineJob;
        }

        public void cancel() {
            // Acquire the Engine lock so that a new request can't get access to a particular EngineJob
            // just after the EngineJob has been cancelled. Without this lock, we'd allow new requests
            // to find the cancelling EngineJob in our Jobs data structure. With this lock, the EngineJob
            // is both cancelled and removed from Jobs atomically.
            synchronized (Engine.this) {
                engineJob.removeCallback(cb);
            }
        }
    }

    private static class LazyDiskCacheProvider implements DecodeJob.DiskCacheProvider {

        private final DiskCache.Factory factory;
        private volatile com.bumptech.glide4110.load.engine.cache.DiskCache diskCache;

        LazyDiskCacheProvider(DiskCache.Factory factory) {
            this.factory = factory;
        }

        @VisibleForTesting
        synchronized void clearDiskCacheIfCreated() {
            if (diskCache == null) {
                return;
            }
            diskCache.clear();
        }

        @Override
        public DiskCache getDiskCache() {
            if (diskCache == null) {
                synchronized (this) {
                    if (diskCache == null) {
                        diskCache = factory.build();
                    }
                    if (diskCache == null) {
                        diskCache = new DiskCacheAdapter();
                    }
                }
            }
            return diskCache;
        }
    }

    @VisibleForTesting
    static class DecodeJobFactory {
        @Synthetic
        final DecodeJob.DiskCacheProvider diskCacheProvider;

        @Synthetic
        final Pools.Pool<DecodeJob<?>> pool =
                FactoryPools.threadSafe(
                        JOB_POOL_SIZE,
                        new FactoryPools.Factory<DecodeJob<?>>() {
                            @Override
                            public DecodeJob<?> create() {
                                return new DecodeJob<>(diskCacheProvider, pool);
                            }
                        });

        private int creationOrder;

        DecodeJobFactory(DecodeJob.DiskCacheProvider diskCacheProvider) {
            this.diskCacheProvider = diskCacheProvider;
        }

        @SuppressWarnings("unchecked")
        <R> DecodeJob<R> build(
                GlideContext glideContext,
                Object model,
                EngineKey loadKey,
                Key signature,
                int width,
                int height,
                Class<?> resourceClass,
                Class<R> transcodeClass,
                Priority priority,
                DiskCacheStrategy diskCacheStrategy,
                Map<Class<?>, Transformation<?>> transformations,
                boolean isTransformationRequired,
                boolean isScaleOnlyOrNoTransform,
                boolean onlyRetrieveFromCache,
                Options options,
                DecodeJob.Callback<R> callback) {
            DecodeJob<R> result = Preconditions.checkNotNull((DecodeJob<R>) pool.acquire());
            return result.init(
                    glideContext,
                    model,
                    loadKey,
                    signature,
                    width,
                    height,
                    resourceClass,
                    transcodeClass,
                    priority,
                    diskCacheStrategy,
                    transformations,
                    isTransformationRequired,
                    isScaleOnlyOrNoTransform,
                    onlyRetrieveFromCache,
                    options,
                    callback,
                    creationOrder++);
        }
    }

    @VisibleForTesting
    static class EngineJobFactory {
        @Synthetic
        final GlideExecutor diskCacheExecutor;
        @Synthetic
        final GlideExecutor sourceExecutor;
        @Synthetic
        final GlideExecutor sourceUnlimitedExecutor;
        @Synthetic
        final GlideExecutor animationExecutor;
        @Synthetic
        final com.bumptech.glide4110.load.engine.EngineJobListener engineJobListener;
        @Synthetic
        final ResourceListener resourceListener;

        @Synthetic
        final Pools.Pool<EngineJob<?>> pool =
                FactoryPools.threadSafe(
                        JOB_POOL_SIZE,
                        new FactoryPools.Factory<EngineJob<?>>() {
                            @Override
                            public EngineJob<?> create() {
                                return new EngineJob<>(
                                        diskCacheExecutor,
                                        sourceExecutor,
                                        sourceUnlimitedExecutor,
                                        animationExecutor,
                                        engineJobListener,
                                        resourceListener,
                                        pool);
                            }
                        });

        EngineJobFactory(
                GlideExecutor diskCacheExecutor,
                GlideExecutor sourceExecutor,
                GlideExecutor sourceUnlimitedExecutor,
                GlideExecutor animationExecutor,
                com.bumptech.glide4110.load.engine.EngineJobListener engineJobListener,
                ResourceListener resourceListener) {
            this.diskCacheExecutor = diskCacheExecutor;
            this.sourceExecutor = sourceExecutor;
            this.sourceUnlimitedExecutor = sourceUnlimitedExecutor;
            this.animationExecutor = animationExecutor;
            this.engineJobListener = engineJobListener;
            this.resourceListener = resourceListener;
        }

        @VisibleForTesting
        void shutdown() {
            Executors.shutdownAndAwaitTermination(diskCacheExecutor);
            Executors.shutdownAndAwaitTermination(sourceExecutor);
            Executors.shutdownAndAwaitTermination(sourceUnlimitedExecutor);
            Executors.shutdownAndAwaitTermination(animationExecutor);
        }

        @SuppressWarnings("unchecked")
        <R> EngineJob<R> build(
                Key key,
                boolean isMemoryCacheable,
                boolean useUnlimitedSourceGeneratorPool,
                boolean useAnimationPool,
                boolean onlyRetrieveFromCache) {
            EngineJob<R> result = Preconditions.checkNotNull((EngineJob<R>) pool.acquire());
            return result.init(
                    key,
                    isMemoryCacheable,
                    useUnlimitedSourceGeneratorPool,
                    useAnimationPool,
                    onlyRetrieveFromCache);
        }
    }
}
