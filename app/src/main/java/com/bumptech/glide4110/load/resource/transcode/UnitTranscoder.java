package com.bumptech.glide4110.load.resource.transcode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide4110.load.Options;
import com.bumptech.glide4110.load.engine.Resource;

/**
 * A simple {@link ResourceTranscoder} that simply returns the given resource.
 *
 * @param <Z> The type of the resource that will be transcoded from and to.
 */
public class UnitTranscoder<Z> implements ResourceTranscoder<Z, Z> {
  private static final UnitTranscoder<?> UNIT_TRANSCODER = new UnitTranscoder<>();

  @SuppressWarnings("unchecked")
  public static <Z> ResourceTranscoder<Z, Z> get() {
    return (ResourceTranscoder<Z, Z>) UNIT_TRANSCODER;
  }

  @Nullable
  @Override
  public com.bumptech.glide4110.load.engine.Resource<Z> transcode(@NonNull Resource<Z> toTranscode, @NonNull Options options) {
    return toTranscode;
  }
}
