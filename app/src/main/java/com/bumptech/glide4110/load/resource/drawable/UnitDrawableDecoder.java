package com.bumptech.glide4110.load.resource.drawable;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide4110.load.Options;
import com.bumptech.glide4110.load.ResourceDecoder;
import com.bumptech.glide4110.load.engine.Resource;

/** Passes through a {@link Drawable} as a {@link Drawable} based {@link com.bumptech.glide4110.load.engine.Resource}. */
public class UnitDrawableDecoder implements ResourceDecoder<Drawable, Drawable> {
  @Override
  public boolean handles(@NonNull Drawable source, @NonNull Options options) {
    return true;
  }

  @Nullable
  @Override
  public Resource<Drawable> decode(
      @NonNull Drawable source, int width, int height, @NonNull Options options) {
    return com.bumptech.glide4110.load.resource.drawable.NonOwnedDrawableResource.newInstance(source);
  }
}
