package com.bumptech.glide4110.manager;

import androidx.annotation.NonNull;

/**
 * A {@link Lifecycle} implementation for tracking and notifying
 * listeners of {@link android.app.Application} lifecycle events.
 *
 * <p>Since there are essentially no {@link android.app.Application} lifecycle events, this class
 * simply defaults to notifying new listeners that they are started.
 */
class ApplicationLifecycle implements Lifecycle {
  @Override
  public void addListener(@NonNull com.bumptech.glide4110.manager.LifecycleListener listener) {
    listener.onStart();
  }

  @Override
  public void removeListener(@NonNull LifecycleListener listener) {
    // Do nothing.
  }
}
