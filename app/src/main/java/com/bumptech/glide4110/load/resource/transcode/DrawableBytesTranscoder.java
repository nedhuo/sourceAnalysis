package com.bumptech.glide4110.load.resource.transcode;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide4110.load.Options;
import com.bumptech.glide4110.load.engine.Resource;
import com.bumptech.glide4110.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide4110.load.resource.bitmap.BitmapResource;
import com.bumptech.glide4110.load.resource.gif.GifDrawable;

/**
 * Obtains {@code byte[]} from {@link BitmapDrawable}s by delegating to a {@link ResourceTranscoder}
 * for {@link Bitmap}s to {@code byte[]}s.
 */
public final class DrawableBytesTranscoder implements ResourceTranscoder<Drawable, byte[]> {
  private final com.bumptech.glide4110.load.engine.bitmap_recycle.BitmapPool bitmapPool;
  private final ResourceTranscoder<Bitmap, byte[]> bitmapBytesTranscoder;
  private final ResourceTranscoder<com.bumptech.glide4110.load.resource.gif.GifDrawable, byte[]> gifDrawableBytesTranscoder;

  public DrawableBytesTranscoder(
      @NonNull BitmapPool bitmapPool,
      @NonNull ResourceTranscoder<Bitmap, byte[]> bitmapBytesTranscoder,
      @NonNull ResourceTranscoder<com.bumptech.glide4110.load.resource.gif.GifDrawable, byte[]> gifDrawableBytesTranscoder) {
    this.bitmapPool = bitmapPool;
    this.bitmapBytesTranscoder = bitmapBytesTranscoder;
    this.gifDrawableBytesTranscoder = gifDrawableBytesTranscoder;
  }

  @Nullable
  @Override
  public com.bumptech.glide4110.load.engine.Resource<byte[]> transcode(
          @NonNull com.bumptech.glide4110.load.engine.Resource<Drawable> toTranscode, @NonNull Options options) {
    Drawable drawable = toTranscode.get();
    if (drawable instanceof BitmapDrawable) {
      return bitmapBytesTranscoder.transcode(
          BitmapResource.obtain(((BitmapDrawable) drawable).getBitmap(), bitmapPool), options);
    } else if (drawable instanceof com.bumptech.glide4110.load.resource.gif.GifDrawable) {
      return gifDrawableBytesTranscoder.transcode(toGifDrawableResource(toTranscode), options);
    }
    return null;
  }

  @SuppressWarnings("unchecked")
  @NonNull
  private static com.bumptech.glide4110.load.engine.Resource<com.bumptech.glide4110.load.resource.gif.GifDrawable> toGifDrawableResource(@NonNull com.bumptech.glide4110.load.engine.Resource<Drawable> resource) {
    return (com.bumptech.glide4110.load.engine.Resource<GifDrawable>) (Resource<?>) resource;
  }
}
