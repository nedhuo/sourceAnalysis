package com.bumptech.glide4110.load.resource.gif;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import com.bumptech.glide4110.load.Transformation;
import com.bumptech.glide4110.load.resource.bitmap.BitmapResource;
import com.bumptech.glide4110.Glide;
import com.bumptech.glide4110.load.engine.Resource;
import com.bumptech.glide4110.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide4110.util.Preconditions;

import java.security.MessageDigest;

/**
 * An {@link Transformation} that wraps a transformation for a {@link
 * Bitmap} and can apply it to every frame of any {@link
 * GifDrawable}.
 */
public class GifDrawableTransformation implements Transformation<GifDrawable> {
  private final Transformation<Bitmap> wrapped;

  public GifDrawableTransformation(Transformation<Bitmap> wrapped) {
    this.wrapped = Preconditions.checkNotNull(wrapped);
  }

  @NonNull
  @Override
  public com.bumptech.glide4110.load.engine.Resource<GifDrawable> transform(
      @NonNull Context context,
      @NonNull com.bumptech.glide4110.load.engine.Resource<GifDrawable> resource,
      int outWidth,
      int outHeight) {
    GifDrawable drawable = resource.get();

    // The drawable needs to be initialized with the correct width and height in order for a view
    // displaying it to end up with the right dimensions. Since our transformations may arbitrarily
    // modify the dimensions of our GIF, here we create a stand in for a frame and pass it to the
    // transformation to see what the final transformed dimensions will be so that our drawable can
    // report the correct intrinsic width and height.
    BitmapPool bitmapPool = Glide.get(context).getBitmapPool();
    Bitmap firstFrame = drawable.getFirstFrame();
    com.bumptech.glide4110.load.engine.Resource<Bitmap> bitmapResource = new BitmapResource(firstFrame, bitmapPool);
    Resource<Bitmap> transformed = wrapped.transform(context, bitmapResource, outWidth, outHeight);
    if (!bitmapResource.equals(transformed)) {
      bitmapResource.recycle();
    }
    Bitmap transformedFrame = transformed.get();

    drawable.setFrameTransformation(wrapped, transformedFrame);
    return resource;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof GifDrawableTransformation) {
      GifDrawableTransformation other = (GifDrawableTransformation) o;
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
