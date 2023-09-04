package com.bumptech.glide4110.load.resource.gif;

import androidx.annotation.NonNull;

import com.bumptech.glide4110.load.resource.drawable.DrawableResource;
import com.bumptech.glide4110.load.engine.Initializable;

/** A resource wrapping an {@link GifDrawable}. */
public class GifDrawableResource extends DrawableResource<GifDrawable> implements Initializable {
  // Public API.
  @SuppressWarnings("WeakerAccess")
  public GifDrawableResource(GifDrawable drawable) {
    super(drawable);
  }

  @NonNull
  @Override
  public Class<GifDrawable> getResourceClass() {
    return GifDrawable.class;
  }

  @Override
  public int getSize() {
    return drawable.getSize();
  }

  @Override
  public void recycle() {
    drawable.stop();
    drawable.recycle();
  }

  @Override
  public void initialize() {
    drawable.getFirstFrame().prepareToDraw();
  }
}
