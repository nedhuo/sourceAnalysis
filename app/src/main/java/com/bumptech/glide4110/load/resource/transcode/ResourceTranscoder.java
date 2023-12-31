package com.bumptech.glide4110.load.resource.transcode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide4110.load.Options;
import com.bumptech.glide4110.load.engine.Resource;

/**
 * Transcodes a resource of one type to a resource of another type.
 *
 * @param <Z> The type of the resource that will be transcoded from.
 * @param <R> The type of the resource that will be transcoded to.
 */
public interface ResourceTranscoder<Z, R> {

  /**
   * Transcodes the given resource to the new resource type and returns the new resource.
   *
   * @param toTranscode The resource to transcode.
   */
  @Nullable
  com.bumptech.glide4110.load.engine.Resource<R> transcode(@NonNull Resource<Z> toTranscode, @NonNull Options options);
}
