package com.bumptech.glide4110.load.resource.bitmap;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide4110.Glide;
import com.bumptech.glide4110.load.engine.Initializable;
import com.bumptech.glide4110.load.engine.Resource;
import com.bumptech.glide4110.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide4110.util.Preconditions;

/**
 * Lazily allocates a {@link BitmapDrawable} from a given {@link
 * Bitmap} on the first call to {@link #get()}.
 */
public final class LazyBitmapDrawableResource implements com.bumptech.glide4110.load.engine.Resource<BitmapDrawable>, com.bumptech.glide4110.load.engine.Initializable {

  private final Resources resources;
  private final com.bumptech.glide4110.load.engine.Resource<Bitmap> bitmapResource;

  /**
   * @deprecated Use {@link #obtain(Resources, com.bumptech.glide4110.load.engine.Resource)} instead, it can be unsafe to extract
   *     {@link Bitmap}s from their wrapped {@link com.bumptech.glide4110.load.engine.Resource}.
   */
  @Deprecated
  public static LazyBitmapDrawableResource obtain(Context context, Bitmap bitmap) {
    return (LazyBitmapDrawableResource)
        obtain(
            context.getResources(),
            BitmapResource.obtain(bitmap, Glide.get(context).getBitmapPool()));
  }

  /**
   * @deprecated Use {@link #obtain(Resources, com.bumptech.glide4110.load.engine.Resource)} instead, it can be unsafe to extract
   *     {@link Bitmap}s from their wrapped {@link com.bumptech.glide4110.load.engine.Resource}.
   */
  @Deprecated
  public static LazyBitmapDrawableResource obtain(
          Resources resources, BitmapPool bitmapPool, Bitmap bitmap) {
    return (LazyBitmapDrawableResource)
        obtain(resources, BitmapResource.obtain(bitmap, bitmapPool));
  }

  @Nullable
  public static com.bumptech.glide4110.load.engine.Resource<BitmapDrawable> obtain(
      @NonNull Resources resources, @Nullable com.bumptech.glide4110.load.engine.Resource<Bitmap> bitmapResource) {
    if (bitmapResource == null) {
      return null;
    }
    return new LazyBitmapDrawableResource(resources, bitmapResource);
  }

  private LazyBitmapDrawableResource(
      @NonNull Resources resources, @NonNull Resource<Bitmap> bitmapResource) {
    this.resources = com.bumptech.glide4110.util.Preconditions.checkNotNull(resources);
    this.bitmapResource = Preconditions.checkNotNull(bitmapResource);
  }

  @NonNull
  @Override
  public Class<BitmapDrawable> getResourceClass() {
    return BitmapDrawable.class;
  }

  @NonNull
  @Override
  public BitmapDrawable get() {
    return new BitmapDrawable(resources, bitmapResource.get());
  }

  @Override
  public int getSize() {
    return bitmapResource.getSize();
  }

  @Override
  public void recycle() {
    bitmapResource.recycle();
  }

  @Override
  public void initialize() {
    if (bitmapResource instanceof com.bumptech.glide4110.load.engine.Initializable) {
      ((Initializable) bitmapResource).initialize();
    }
  }
}
