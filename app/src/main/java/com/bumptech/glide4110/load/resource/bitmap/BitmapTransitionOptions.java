package com.bumptech.glide4110.load.resource.bitmap;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

import com.bumptech.glide4110.GenericTransitionOptions;
import com.bumptech.glide4110.TransitionOptions;
import com.bumptech.glide4110.request.transition.BitmapTransitionFactory;
import com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide4110.request.transition.TransitionFactory;

/** Contains {@link Bitmap} specific animation options. */
// Public API.
@SuppressWarnings({"unused", "WeakerAccess"})
public final class BitmapTransitionOptions
    extends TransitionOptions<BitmapTransitionOptions, Bitmap> {

  /**
   * Returns a {@link BitmapTransitionOptions} object that enables a cross fade animation.
   *
   * @see #crossFade()
   */
  @NonNull
  public static BitmapTransitionOptions withCrossFade() {
    return new BitmapTransitionOptions().crossFade();
  }

  /**
   * Returns a {@link BitmapTransitionOptions} object that enables a cross fade animation.
   *
   * @see #crossFade(int)
   */
  @NonNull
  public static BitmapTransitionOptions withCrossFade(int duration) {
    return new BitmapTransitionOptions().crossFade(duration);
  }

  /**
   * Returns a {@link BitmapTransitionOptions} object that enables a cross fade animation.
   *
   * @see #crossFade(com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory)
   */
  @NonNull
  public static BitmapTransitionOptions withCrossFade(
      @NonNull com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory drawableCrossFadeFactory) {
    return new BitmapTransitionOptions().crossFade(drawableCrossFadeFactory);
  }

  /**
   * Returns a {@link BitmapTransitionOptions} object that enables a cross fade animation.
   *
   * @see #crossFade(com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory.Builder)
   */
  @NonNull
  public static BitmapTransitionOptions withCrossFade(
      @NonNull com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory.Builder builder) {
    return new BitmapTransitionOptions().crossFade(builder);
  }

  /**
   * Returns a {@link BitmapTransitionOptions} object that enables a any animation that is possible
   * on drawables.
   *
   * @see #transitionUsing(com.bumptech.glide4110.request.transition.TransitionFactory)
   */
  @NonNull
  public static BitmapTransitionOptions withWrapped(
      @NonNull com.bumptech.glide4110.request.transition.TransitionFactory<Drawable> drawableCrossFadeFactory) {
    return new BitmapTransitionOptions().transitionUsing(drawableCrossFadeFactory);
  }

  /**
   * Returns a {@link BitmapTransitionOptions} object that uses the given transition factory.
   *
   * @see GenericTransitionOptions#with(com.bumptech.glide4110.request.transition.TransitionFactory)
   */
  @NonNull
  public static BitmapTransitionOptions with(@NonNull com.bumptech.glide4110.request.transition.TransitionFactory<Bitmap> transitionFactory) {
    return new BitmapTransitionOptions().transition(transitionFactory);
  }

  /**
   * Enables a cross fade animation between both the placeholder and the first resource and between
   * subsequent resources (if thumbnails are used).
   */
  @NonNull
  public BitmapTransitionOptions crossFade() {
    return crossFade(new com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory.Builder());
  }

  /**
   * Enables a cross fade animation between both the placeholder and the first resource and between
   * subsequent resources (if thumbnails are used).
   *
   * @param duration The duration of the animation, see {@code
   *     DrawableCrossFadeFactory.Builder(int)}.
   * @see com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory.Builder
   */
  @NonNull
  public BitmapTransitionOptions crossFade(int duration) {
    return crossFade(new com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory.Builder(duration));
  }

  /**
   * Enables a cross fade animation between both the placeholder and the first resource and between
   * subsequent resources (if thumbnails are used).
   */
  @NonNull
  public BitmapTransitionOptions crossFade(
      @NonNull com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory drawableCrossFadeFactory) {
    return transitionUsing(drawableCrossFadeFactory);
  }

  /** Enables a any Drawable based animation to run on Bitmaps as well. */
  @NonNull
  public BitmapTransitionOptions transitionUsing(
      @NonNull TransitionFactory<Drawable> drawableCrossFadeFactory) {
    return transition(new BitmapTransitionFactory(drawableCrossFadeFactory));
  }

  /**
   * Enables a cross fade animation between both the placeholder and the first resource and between
   * subsequent resources (if thumbnails are used).
   */
  @NonNull
  public BitmapTransitionOptions crossFade(@NonNull DrawableCrossFadeFactory.Builder builder) {
    return transitionUsing(builder.build());
  }
}
