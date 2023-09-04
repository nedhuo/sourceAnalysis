package com.bumptech.glide4110.load.resource.transcode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide4110.load.Options;
import com.bumptech.glide4110.load.engine.Resource;
import com.bumptech.glide4110.load.resource.bytes.BytesResource;
import com.bumptech.glide4110.load.resource.gif.GifDrawable;
import com.bumptech.glide4110.util.ByteBufferUtil;

import java.nio.ByteBuffer;

/**
 * An {@link ResourceTranscoder} that converts {@link
 * com.bumptech.glide4110.load.resource.gif.GifDrawable} into bytes by obtaining the original bytes of
 * the GIF from the {@link com.bumptech.glide4110.load.resource.gif.GifDrawable}.
 */
public class GifDrawableBytesTranscoder implements ResourceTranscoder<com.bumptech.glide4110.load.resource.gif.GifDrawable, byte[]> {
  @Nullable
  @Override
  public com.bumptech.glide4110.load.engine.Resource<byte[]> transcode(
          @NonNull Resource<com.bumptech.glide4110.load.resource.gif.GifDrawable> toTranscode, @NonNull Options options) {
    GifDrawable gifData = toTranscode.get();
    ByteBuffer byteBuffer = gifData.getBuffer();
    return new BytesResource(ByteBufferUtil.toBytes(byteBuffer));
  }
}
