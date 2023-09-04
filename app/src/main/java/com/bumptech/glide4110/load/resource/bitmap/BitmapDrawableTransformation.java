package com.bumptech.glide4110.load.resource.bitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import com.bumptech.glide4110.load.Transformation;
import com.bumptech.glide4110.load.engine.Resource;
import com.bumptech.glide4110.util.Preconditions;

import java.security.MessageDigest;

/**
 * Transforms {@link BitmapDrawable}s.
 *
 * @deprecated Use {@link DrawableTransformation} instead.
 */
@Deprecated
public class BitmapDrawableTransformation implements Transformation<BitmapDrawable> {

  private final Transformation<Drawable> wrapped;

  // Public API.
  @SuppressWarnings("WeakerAccess")
  public BitmapDrawableTransformation(Transformation<Bitmap> wrapped) {
    this.wrapped =
        Preconditions.checkNotNull(new DrawableTransformation(wrapped, /*isRequired=*/ false));
  }

  @NonNull
  @Override
  public com.bumptech.glide4110.load.engine.Resource<BitmapDrawable> transform(
      @NonNull Context context,
      @NonNull com.bumptech.glide4110.load.engine.Resource<BitmapDrawable> drawableResourceToTransform,
      int outWidth,
      int outHeight) {
    com.bumptech.glide4110.load.engine.Resource<Drawable> toTransform = convertToDrawableResource(drawableResourceToTransform);
    com.bumptech.glide4110.load.engine.Resource<Drawable> transformed = wrapped.transform(context, toTransform, outWidth, outHeight);
    return convertToBitmapDrawableResource(transformed);
  }

  @SuppressWarnings("unchecked")
  private static com.bumptech.glide4110.load.engine.Resource<BitmapDrawable> convertToBitmapDrawableResource(
      com.bumptech.glide4110.load.engine.Resource<Drawable> resource) {
    if (!(resource.get() instanceof BitmapDrawable)) {
      throw new IllegalArgumentException(
          "Wrapped transformation unexpectedly returned a non BitmapDrawable resource: "
              + resource.get());
    }
    return (com.bumptech.glide4110.load.engine.Resource<BitmapDrawable>) (com.bumptech.glide4110.load.engine.Resource<?>) resource;
  }

  @SuppressWarnings("unchecked")
  private static com.bumptech.glide4110.load.engine.Resource<Drawable> convertToDrawableResource(com.bumptech.glide4110.load.engine.Resource<BitmapDrawable> toConvert) {
    return (com.bumptech.glide4110.load.engine.Resource<Drawable>) (Resource<? extends Drawable>) toConvert;
  }

  @SuppressWarnings("deprecation")
  @Override
  public boolean equals(Object o) {
    if (o instanceof BitmapDrawableTransformation) {
      BitmapDrawableTransformation other = (BitmapDrawableTransformation) o;
      return wrapped.equals(other.wrapped);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return wrapped.hashCode();
  }

  @Override
  public void updateDiskCacheKey(@NonNull MessageDigest messageDigest) {
    wrapped.updateDiskCacheKey(messageDigest);
  }
}
