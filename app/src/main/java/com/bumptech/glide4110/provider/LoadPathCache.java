package com.bumptech.glide4110.provider;

import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import com.bumptech.glide4110.load.ResourceDecoder;
import com.bumptech.glide4110.load.engine.DecodePath;
import com.bumptech.glide4110.load.engine.LoadPath;
import com.bumptech.glide4110.load.resource.transcode.UnitTranscoder;
import com.bumptech.glide4110.util.MultiClassKey;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Maintains a cache of data, resource, and transcode classes to available {@link
 * LoadPath}s capable of decoding with the requested types.
 */
public class LoadPathCache {
  private static final LoadPath<?, ?, ?> NO_PATHS_SIGNAL =
      new LoadPath<>(
          Object.class,
          Object.class,
          Object.class,
          Collections.singletonList(
              new DecodePath<>(
                  Object.class,
                  Object.class,
                  Object.class,
                  Collections.<ResourceDecoder<Object, Object>>emptyList(),
                  new UnitTranscoder<>(),
                  /*listPool=*/ null)),
          /*listPool=*/ null);

  private final ArrayMap<com.bumptech.glide4110.util.MultiClassKey, LoadPath<?, ?, ?>> cache = new ArrayMap<>();
  private final AtomicReference<com.bumptech.glide4110.util.MultiClassKey> keyRef = new AtomicReference<>();

  /**
   * Returns {@code} true if the given {@link LoadPath} is the signal object returned from {@link
   * #get(Class, Class, Class)} that indicates that we've previously found that there are no
   * available paths to load the requested resources and {@code false} otherwise.
   */
  public boolean isEmptyLoadPath(@Nullable LoadPath<?, ?, ?> path) {
    return NO_PATHS_SIGNAL.equals(path);
  }

  /**
   * May return {@link #NO_PATHS_SIGNAL} to indicate that we've previously found that there are 0
   * available load paths for the requested types. Callers must check using {@link
   * #isEmptyLoadPath(LoadPath)} before using any load path returned by this method.
   */
  @SuppressWarnings("unchecked")
  @Nullable
  public <Data, TResource, Transcode> LoadPath<Data, TResource, Transcode> get(
      Class<Data> dataClass, Class<TResource> resourceClass, Class<Transcode> transcodeClass) {
    com.bumptech.glide4110.util.MultiClassKey key = getKey(dataClass, resourceClass, transcodeClass);
    LoadPath<?, ?, ?> result;
    synchronized (cache) {
      result = cache.get(key);
    }
    keyRef.set(key);

    return (LoadPath<Data, TResource, Transcode>) result;
  }

  public void put(
      Class<?> dataClass,
      Class<?> resourceClass,
      Class<?> transcodeClass,
      @Nullable LoadPath<?, ?, ?> loadPath) {
    synchronized (cache) {
      cache.put(
          new com.bumptech.glide4110.util.MultiClassKey(dataClass, resourceClass, transcodeClass),
          loadPath != null ? loadPath : NO_PATHS_SIGNAL);
    }
  }

  private com.bumptech.glide4110.util.MultiClassKey getKey(
      Class<?> dataClass, Class<?> resourceClass, Class<?> transcodeClass) {
    com.bumptech.glide4110.util.MultiClassKey key = keyRef.getAndSet(null);
    if (key == null) {
      key = new MultiClassKey();
    }
    key.set(dataClass, resourceClass, transcodeClass);
    return key;
  }
}
