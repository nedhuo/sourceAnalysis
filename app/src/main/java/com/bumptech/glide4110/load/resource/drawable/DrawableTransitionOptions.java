package com.bumptech.glide4110.load.resource.drawable;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;

import com.bumptech.glide4110.GenericTransitionOptions;
import com.bumptech.glide4110.TransitionOptions;
import com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory;
import com.bumptech.glide4110.request.transition.TransitionFactory;

/** Contains {@link Drawable} specific animation options. */
// Public API.
@SuppressWarnings("WeakerAccess")
public final class DrawableTransitionOptions
    extends TransitionOptions<DrawableTransitionOptions, Drawable> {

  /**
   * Returns a {@link DrawableTransitionOptions} object that enables a cross fade animation.
   *
   * @see #crossFade()
   */
  @NonNull
  public static DrawableTransitionOptions withCrossFade() {
    return new DrawableTransitionOptions().crossFade();
  }

  /**
   * Returns a {@link DrawableTransitionOptions} object that enables a cross fade animation.
   *
   * @see #crossFade(int)
   */
  @NonNull
  public static DrawableTransitionOptions withCrossFade(int duration) {
    return new DrawableTransitionOptions().crossFade(duration);
  }

  /**
   * Returns a {@link DrawableTransitionOptions} object that enables a cross fade animation.
   *
   * @see #crossFade(com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory)
   */
  @NonNull
  public static DrawableTransitionOptions withCrossFade(
      @NonNull com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory drawableCrossFadeFactory) {
    return new DrawableTransitionOptions().crossFade(drawableCrossFadeFactory);
  }

  /**
   * Returns a {@link DrawableTransitionOptions} object that enables a cross fade animation.
   *
   * @see #crossFade(com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory.Builder)
   */
  @NonNull
  public static DrawableTransitionOptions withCrossFade(
      @NonNull com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory.Builder builder) {
    return new DrawableTransitionOptions().crossFade(builder);
  }

  /**
   * Returns a {@link DrawableTransitionOptions} object that uses the given transition factory.
   *
   * @see GenericTransitionOptions#with(com.bumptech.glide4110.request.transition.TransitionFactory)
   */
  @NonNull
  public static DrawableTransitionOptions with(
      @NonNull TransitionFactory<Drawable> transitionFactory) {
    return new DrawableTransitionOptions().transition(transitionFactory);
  }

  /**
   * Enables a cross fade animation between both the placeholder and the first resource and between
   * subsequent resources (if thumbnails are used).
   */
  @NonNull
  public DrawableTransitionOptions crossFade() {
    return crossFade(new com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory.Builder());
  }

  /**
   * Enables a cross fade animation between both the placeholder and the first resource and between
   * subsequent resources (if thumbnails are used).
   *
   * @param duration The duration of the animation, see {@code
   *     DrawableCrossFadeFactory.Builder(int)}
   * @see com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory.Builder
   */
  @NonNull
  public DrawableTransitionOptions crossFade(int duration) {
    return crossFade(new com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory.Builder(duration));
  }

  /**
   * Enables a cross fade animation between both the placeholder and the first resource and between
   * subsequent resources (if thumbnails are used).
   */
  @NonNull
  public DrawableTransitionOptions crossFade(
      @NonNull com.bumptech.glide4110.request.transition.DrawableCrossFadeFactory drawableCrossFadeFactory) {
    return transition(drawableCrossFadeFactory);
  }

  /**
   * Enables a cross fade animation between both the placeholder and the first resource and between
   * subsequent resources (if thumbnails are used).
   */
  @NonNull
  public DrawableTransitionOptions crossFade(@NonNull DrawableCrossFadeFactory.Builder builder) {
    return crossFade(builder.build());
  }
}
