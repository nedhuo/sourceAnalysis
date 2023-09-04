package com.bumptech.glide4110.load.resource.bitmap;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.ImageDecoder.Source;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.bumptech.glide4110.load.Options;
import com.bumptech.glide4110.load.ResourceDecoder;
import com.bumptech.glide4110.load.engine.Resource;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * {@link ByteBuffer} specific implementation of {@link
 * ByteBufferBitmapImageDecoderResourceDecoder}.
 */
@RequiresApi(api = 28)
public final class ByteBufferBitmapImageDecoderResourceDecoder
    implements ResourceDecoder<ByteBuffer, Bitmap> {
  private final com.bumptech.glide4110.load.resource.bitmap.BitmapImageDecoderResourceDecoder wrapped = new BitmapImageDecoderResourceDecoder();

  @Override
  public boolean handles(@NonNull ByteBuffer source, @NonNull Options options) throws IOException {
    return true;
  }

  @Nullable
  @Override
  public Resource<Bitmap> decode(
      @NonNull ByteBuffer buffer, int width, int height, @NonNull Options options)
      throws IOException {
    Source source = ImageDecoder.createSource(buffer);
    return wrapped.decode(source, width, height, options);
  }
}
