package com.bumptech.glide4110.load.engine;

import androidx.annotation.VisibleForTesting;
import com.bumptech.glide4110.load.Key;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class Jobs {
  private final Map<Key, com.bumptech.glide4110.load.engine.EngineJob<?>> jobs = new HashMap<>();
  private final Map<Key, com.bumptech.glide4110.load.engine.EngineJob<?>> onlyCacheJobs = new HashMap<>();

  @VisibleForTesting
  Map<Key, com.bumptech.glide4110.load.engine.EngineJob<?>> getAll() {
    return Collections.unmodifiableMap(jobs);
  }

  com.bumptech.glide4110.load.engine.EngineJob<?> get(Key key, boolean onlyRetrieveFromCache) {
    return getJobMap(onlyRetrieveFromCache).get(key);
  }

  void put(Key key, com.bumptech.glide4110.load.engine.EngineJob<?> job) {
    getJobMap(job.onlyRetrieveFromCache()).put(key, job);
  }

  void removeIfCurrent(Key key, com.bumptech.glide4110.load.engine.EngineJob<?> expected) {
    Map<Key, com.bumptech.glide4110.load.engine.EngineJob<?>> jobMap = getJobMap(expected.onlyRetrieveFromCache());
    if (expected.equals(jobMap.get(key))) {
      jobMap.remove(key);
    }
  }

  private Map<Key, com.bumptech.glide4110.load.engine.EngineJob<?>> getJobMap(boolean onlyRetrieveFromCache) {
    return onlyRetrieveFromCache ? onlyCacheJobs : jobs;
  }
}
