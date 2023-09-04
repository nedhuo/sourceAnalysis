package com.bumptech.glide4110.request;

import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import androidx.annotation.Nullable;
import com.bumptech.glide4110.RequestBuilder;
import com.bumptech.glide4110.load.DataSource;
import com.bumptech.glide4110.load.engine.GlideException;
import com.bumptech.glide4110.request.target.Target;
import com.bumptech.glide4110.request.transition.Transition;

/**
 * A class for monitoring the status of a request while images load.
 *
 * <p>All methods in this interface will be called from a background thread if the {@code
 * RequestListener} is added to a request that is started with {@link RequestBuilder#submit()},
 * {@link RequestBuilder#submit(int, int)}, or {@link RequestBuilder#into(int, int)}. Those methods
 * no longer post results back to the main thread to avoid the unnecessary thread interactions and
 * corresponding latency. As a side affect though, listeners added to those requests are no longer
 * called on the main thread. {@code RequestListeners} added to requests started with {@link
 * RequestBuilder#into(com.bumptech.glide4110.request.target.Target)} or {@link RequestBuilder#into(ImageView)} will continue to be called
 * back on the main thread.
 *
 * @param <R> The type of resource being loaded.
 */
public interface RequestListener<R> {

  /**
   * Called when an exception occurs during a load, immediately before {@link
   * com.bumptech.glide4110.request.target.Target#onLoadFailed(Drawable)}. Will only be called if we currently want to display an image
   * for the given model in the given target. It is recommended to create a single instance per
   * activity/fragment rather than instantiate a new object for each call to {@code
   * Glide.with(fragment/activity).load()} to avoid object churn.
   *
   * <p>It is not safe to reload this or a different model in this callback. If you need to do so
   * use {@link RequestBuilder#error(RequestBuilder)} instead.
   *
   * <p>Although you can't start an entirely new load, it is safe to change what is displayed in the
   * {@link com.bumptech.glide4110.request.target.Target} at this point, as long as you return {@code true} from the method to prevent
   * {@link com.bumptech.glide4110.request.target.Target#onLoadFailed(Drawable)} from being called.
   *
   * <p>For threading guarantees, see the class comment.
   *
   * <p>For example:
   *
   * <pre>{@code
   * public boolean onLoadFailed(Exception e, T model, Target target, boolean isFirstResource) {
   *     target.setPlaceholder(R.drawable.a_specific_error_for_my_exception);
   *     return true; // Prevent onLoadFailed from being called on the Target.
   * }
   * }</pre>
   *
   * @param e The maybe {@code null} exception containing information about why the request failed.
   * @param model The model we were trying to load when the exception occurred.
   * @param target The {@link com.bumptech.glide4110.request.target.Target} we were trying to load the image into.
   * @param isFirstResource {@code true} if this exception is for the first resource to load.
   * @return {@code true} to prevent {@link com.bumptech.glide4110.request.target.Target#onLoadFailed(Drawable)} from being called on
   *     {@code target}, typically because the listener wants to update the {@code target} or the
   *     object the {@code target} wraps itself or {@code false} to allow {@link
   *     com.bumptech.glide4110.request.target.Target#onLoadFailed(Drawable)} to be called on {@code target}.
   */
  boolean onLoadFailed(
          @Nullable GlideException e, Object model, com.bumptech.glide4110.request.target.Target<R> target, boolean isFirstResource);

  /**
   * Called when a load completes successfully, immediately before {@link
   * com.bumptech.glide4110.request.target.Target#onResourceReady(Object, Transition)}.
   *
   * <p>For threading guarantees, see the class comment.
   *
   * @param resource The resource that was loaded for the target.
   * @param model The specific model that was used to load the image.
   * @param target The target the model was loaded into.
   * @param dataSource The {@link DataSource} the resource was loaded from.
   * @param isFirstResource {@code true} if this is the first resource to in this load to be loaded
   *     into the target. For example when loading a thumbnail and a full-sized image, this will be
   *     {@code true} for the first image to load and {@code false} for the second.
   * @return {@code true} to prevent {@link com.bumptech.glide4110.request.target.Target#onLoadFailed(Drawable)} from being called on
   *     {@code target}, typically because the listener wants to update the {@code target} or the
   *     object the {@code target} wraps itself or {@code false} to allow {@link
   *     com.bumptech.glide4110.request.target.Target#onLoadFailed(Drawable)} to be called on {@code target}.
   */
  boolean onResourceReady(
          R resource, Object model, Target<R> target, DataSource dataSource, boolean isFirstResource);
}
