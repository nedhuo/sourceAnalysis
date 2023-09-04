package com.bumptech.glide4110.request.transition;

import com.bumptech.glide4110.load.DataSource;

/**
 * A factory class that can produce different {@link com.bumptech.glide4110.request.transition.Transition}s based on the state of the request.
 *
 * @param <R> The type of resource that needs to be animated into the target.
 */
public interface TransitionFactory<R> {

  /**
   * Returns a new {@link com.bumptech.glide4110.request.transition.Transition}.
   *
   * @param dataSource The {@link DataSource} the resource was loaded from.
   * @param isFirstResource True if this is the first resource to be loaded into the target.
   */
  Transition<R> build(DataSource dataSource, boolean isFirstResource);
}
