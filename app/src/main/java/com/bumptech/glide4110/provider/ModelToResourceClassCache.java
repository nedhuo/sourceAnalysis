package com.bumptech.glide4110.provider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import com.bumptech.glide4110.util.MultiClassKey;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Maintains a cache of Model + Resource class to a set of registered resource classes that are
 * subclasses of the resource class that can be decoded from the model class.
 */
public class ModelToResourceClassCache {
  private final AtomicReference<com.bumptech.glide4110.util.MultiClassKey> resourceClassKeyRef = new AtomicReference<>();
  private final ArrayMap<com.bumptech.glide4110.util.MultiClassKey, List<Class<?>>> registeredResourceClassCache =
      new ArrayMap<>();

  @Nullable
  public List<Class<?>> get(
      @NonNull Class<?> modelClass,
      @NonNull Class<?> resourceClass,
      @NonNull Class<?> transcodeClass) {
    com.bumptech.glide4110.util.MultiClassKey key = resourceClassKeyRef.getAndSet(null);
    if (key == null) {
      key = new com.bumptech.glide4110.util.MultiClassKey(modelClass, resourceClass, transcodeClass);
    } else {
      key.set(modelClass, resourceClass, transcodeClass);
    }
    final List<Class<?>> result;
    synchronized (registeredResourceClassCache) {
      result = registeredResourceClassCache.get(key);
    }
    resourceClassKeyRef.set(key);
    return result;
  }

  public void put(
      @NonNull Class<?> modelClass,
      @NonNull Class<?> resourceClass,
      @NonNull Class<?> transcodeClass,
      @NonNull List<Class<?>> resourceClasses) {
    synchronized (registeredResourceClassCache) {
      registeredResourceClassCache.put(
          new MultiClassKey(modelClass, resourceClass, transcodeClass), resourceClasses);
    }
  }

  public void clear() {
    synchronized (registeredResourceClassCache) {
      registeredResourceClassCache.clear();
    }
  }
}
