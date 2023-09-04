package com.bumptech.glide4110.load.resource.bitmap;

import android.content.Context;
import android.os.ParcelFileDescriptor;

import com.bumptech.glide4110.Glide;
import com.bumptech.glide4110.load.ResourceDecoder;
import com.bumptech.glide4110.load.engine.bitmap_recycle.BitmapPool;

/**
 * An {@link ResourceDecoder} that can decode a thumbnail frame {@link
 * android.graphics.Bitmap} from a {@link ParcelFileDescriptor} containing a video.
 *
 * @see android.media.MediaMetadataRetriever
 * @deprecated Use {@link VideoDecoder#parcel(com.bumptech.glide4110.load.engine.bitmap_recycle.BitmapPool)} instead. This class may be removed and
 *     {@link VideoDecoder} may become final in a future version of Glide.
 */
@Deprecated
public class VideoBitmapDecoder extends VideoDecoder<ParcelFileDescriptor> {

  @SuppressWarnings("unused")
  public VideoBitmapDecoder(Context context) {
    this(Glide.get(context).getBitmapPool());
  }

  // Public API
  @SuppressWarnings("WeakerAccess")
  public VideoBitmapDecoder(BitmapPool bitmapPool) {
    super(bitmapPool, new ParcelFileDescriptorInitializer());
  }
}
