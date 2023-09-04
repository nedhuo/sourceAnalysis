package com.bumptech.glide4110.load.resource.bitmap;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import com.bumptech.glide4110.load.Options;
import com.bumptech.glide4110.load.ResourceDecoder;
import com.bumptech.glide4110.util.Util;
import com.bumptech.glide4110.load.engine.Resource;

/**
 * Passes through a (hopefully) non-owned {@link Bitmap} as a {@link Bitmap} based {@link com.bumptech.glide4110.load.engine.Resource}
 * so that the given {@link Bitmap} is not recycled.
 */
public final class UnitBitmapDecoder implements ResourceDecoder<Bitmap, Bitmap> {

  @Override
  public boolean handles(@NonNull Bitmap source, @NonNull Options options) {
    return true;
  }

  @Override
  public com.bumptech.glide4110.load.engine.Resource<Bitmap> decode(
      @NonNull Bitmap source, int width, int height, @NonNull Options options) {
    return new NonOwnedBitmapResource(source);
  }

  private static final class NonOwnedBitmapResource implements Resource<Bitmap> {

    private final Bitmap bitmap;

    NonOwnedBitmapResource(@NonNull Bitmap bitmap) {
      this.bitmap = bitmap;
    }

    @NonNull
    @Override
    public Class<Bitmap> getResourceClass() {
      return Bitmap.class;
    }

    @NonNull
    @Override
    public Bitmap get() {
      return bitmap;
    }

    @Override
    public int getSize() {
      return Util.getBitmapByteSize(bitmap);
    }

    @Override
    public void recycle() {
      // Do nothing.
    }
  }
}
