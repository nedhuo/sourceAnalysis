package com.bumptech.glide4110.load.engine.cache;

import android.content.Context;

import java.io.File;

/**
 * Creates an {@link com.bumptech.glide.disklrucache.DiskLruCache} based disk cache in the internal
 * disk cache directory.
 * 创建一个基于LRU的磁盘缓存
 */
// Public API.
@SuppressWarnings({"WeakerAccess", "unused"})
public final class InternalCacheDiskCacheFactory extends DiskLruCacheFactory {

    public InternalCacheDiskCacheFactory(Context context) {
        this(
                context,
                DiskCache.Factory.DEFAULT_DISK_CACHE_DIR,
                DiskCache.Factory.DEFAULT_DISK_CACHE_SIZE);
    }

    public InternalCacheDiskCacheFactory(Context context, long diskCacheSize) {
        this(context, DiskCache.Factory.DEFAULT_DISK_CACHE_DIR, diskCacheSize);
    }

    public InternalCacheDiskCacheFactory(
            final Context context, final String diskCacheName, long diskCacheSize) {
        super(
                new CacheDirectoryGetter() {
                    @Override
                    public File getCacheDirectory() {
                        File cacheDirectory = context.getCacheDir();
                        if (cacheDirectory == null) {
                            return null;
                        }
                        if (diskCacheName != null) {
                            return new File(cacheDirectory, diskCacheName);
                        }
                        return cacheDirectory;
                    }
                },
                diskCacheSize);
    }
}
