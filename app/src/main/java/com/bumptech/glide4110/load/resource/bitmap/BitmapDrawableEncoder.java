package com.bumptech.glide4110.load.resource.bitmap;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import androidx.annotation.NonNull;
import com.bumptech.glide4110.load.EncodeStrategy;
import com.bumptech.glide4110.load.Options;
import com.bumptech.glide4110.load.ResourceEncoder;
import com.bumptech.glide4110.load.engine.Resource;
import com.bumptech.glide4110.load.engine.bitmap_recycle.BitmapPool;

import java.io.File;

/** Encodes {@link BitmapDrawable}s. */
public class BitmapDrawableEncoder implements ResourceEncoder<BitmapDrawable> {

  private final com.bumptech.glide4110.load.engine.bitmap_recycle.BitmapPool bitmapPool;
  private final ResourceEncoder<Bitmap> encoder;

  public BitmapDrawableEncoder(BitmapPool bitmapPool, ResourceEncoder<Bitmap> encoder) {
    this.bitmapPool = bitmapPool;
    this.encoder = encoder;
  }

  @Override
  public boolean encode(
          @NonNull Resource<BitmapDrawable> data, @NonNull File file, @NonNull Options options) {
    return encoder.encode(new BitmapResource(data.get().getBitmap(), bitmapPool), file, options);
  }

  @NonNull
  @Override
  public EncodeStrategy getEncodeStrategy(@NonNull Options options) {
    return encoder.getEncodeStrategy(options);
  }
}
