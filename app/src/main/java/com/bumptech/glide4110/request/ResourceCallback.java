package com.bumptech.glide4110.request;

import com.bumptech.glide4110.load.DataSource;
import com.bumptech.glide4110.load.engine.GlideException;
import com.bumptech.glide4110.load.engine.Resource;

/**
 * A callback that listens for when a resource load completes successfully or fails due to an
 * exception.
 */
public interface ResourceCallback {

  /**
   * Called when a resource is successfully loaded.
   *
   * @param resource The loaded resource.
   */
  void onResourceReady(Resource<?> resource, DataSource dataSource);

  /**
   * Called when a resource fails to load successfully.
   *
   * @param e a non-null {@link GlideException}.
   */
  void onLoadFailed(GlideException e);

  /** Returns the lock to use when notifying individual requests. */
  Object getLock();
}
