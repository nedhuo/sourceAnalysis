package com.bumptech.glide4110;

import android.content.Context;
import android.content.ContextWrapper;
import android.widget.ImageView;
import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.bumptech.glide4110.load.engine.Engine;
import com.bumptech.glide4110.load.engine.GlideException;
import com.bumptech.glide4110.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide4110.request.RequestListener;
import com.bumptech.glide4110.request.RequestOptions;
import com.bumptech.glide4110.request.target.ImageViewTargetFactory;
import com.bumptech.glide4110.request.target.ViewTarget;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Global context for all loads in Glide containing and exposing the various registries and classes
 * required to load resources.
 */
public class GlideContext extends ContextWrapper {
  @VisibleForTesting
  static final com.bumptech.glide4110.TransitionOptions<?, ?> DEFAULT_TRANSITION_OPTIONS =
      new GenericTransitionOptions<>();

  private final ArrayPool arrayPool;
  private final com.bumptech.glide4110.Registry registry;
  private final com.bumptech.glide4110.request.target.ImageViewTargetFactory imageViewTargetFactory;
  private final Glide.RequestOptionsFactory defaultRequestOptionsFactory;
  private final List<com.bumptech.glide4110.request.RequestListener<Object>> defaultRequestListeners;
  private final Map<Class<?>, com.bumptech.glide4110.TransitionOptions<?, ?>> defaultTransitionOptions;
  private final Engine engine;
  private final boolean isLoggingRequestOriginsEnabled;
  private final int logLevel;

  @Nullable
  @GuardedBy("this")
  private com.bumptech.glide4110.request.RequestOptions defaultRequestOptions;

  public GlideContext(
      @NonNull Context context,
      @NonNull ArrayPool arrayPool,
      @NonNull com.bumptech.glide4110.Registry registry,
      @NonNull ImageViewTargetFactory imageViewTargetFactory,
      @NonNull Glide.RequestOptionsFactory defaultRequestOptionsFactory,
      @NonNull Map<Class<?>, com.bumptech.glide4110.TransitionOptions<?, ?>> defaultTransitionOptions,
      @NonNull List<com.bumptech.glide4110.request.RequestListener<Object>> defaultRequestListeners,
      @NonNull Engine engine,
      boolean isLoggingRequestOriginsEnabled,
      int logLevel) {
    super(context.getApplicationContext());
    this.arrayPool = arrayPool;
    this.registry = registry;
    this.imageViewTargetFactory = imageViewTargetFactory;
    this.defaultRequestOptionsFactory = defaultRequestOptionsFactory;
    this.defaultRequestListeners = defaultRequestListeners;
    this.defaultTransitionOptions = defaultTransitionOptions;
    this.engine = engine;
    this.isLoggingRequestOriginsEnabled = isLoggingRequestOriginsEnabled;
    this.logLevel = logLevel;
  }

  public List<RequestListener<Object>> getDefaultRequestListeners() {
    return defaultRequestListeners;
  }

  public synchronized RequestOptions getDefaultRequestOptions() {
    if (defaultRequestOptions == null) {
      defaultRequestOptions = defaultRequestOptionsFactory.build().lock();
    }

    return defaultRequestOptions;
  }

  @SuppressWarnings("unchecked")
  @NonNull
  public <T> com.bumptech.glide4110.TransitionOptions<?, T> getDefaultTransitionOptions(@NonNull Class<T> transcodeClass) {
    com.bumptech.glide4110.TransitionOptions<?, ?> result = defaultTransitionOptions.get(transcodeClass);
    if (result == null) {
      for (Entry<Class<?>, com.bumptech.glide4110.TransitionOptions<?, ?>> value : defaultTransitionOptions.entrySet()) {
        if (value.getKey().isAssignableFrom(transcodeClass)) {
          result = value.getValue();
        }
      }
    }
    if (result == null) {
      result = DEFAULT_TRANSITION_OPTIONS;
    }
    return (TransitionOptions<?, T>) result;
  }

  @NonNull
  public <X> ViewTarget<ImageView, X> buildImageViewTarget(
      @NonNull ImageView imageView, @NonNull Class<X> transcodeClass) {
    return imageViewTargetFactory.buildTarget(imageView, transcodeClass);
  }

  @NonNull
  public Engine getEngine() {
    return engine;
  }

  @NonNull
  public Registry getRegistry() {
    return registry;
  }

  public int getLogLevel() {
    return logLevel;
  }

  @NonNull
  public ArrayPool getArrayPool() {
    return arrayPool;
  }

  /**
   * Returns {@code true} if Glide should populate {@link
   * GlideException#setOrigin(Exception)} for failed requests.
   *
   * <p>This is an experimental API that may be removed in the future.
   */
  public boolean isLoggingRequestOriginsEnabled() {
    return isLoggingRequestOriginsEnabled;
  }
}
