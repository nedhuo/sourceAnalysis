package com.bumptech.glide4110.load.resource.bytes;

import androidx.annotation.NonNull;

import com.bumptech.glide4110.load.data.DataRewinder;

import java.nio.ByteBuffer;

/** Rewinds {@link ByteBuffer}s. */
public class ByteBufferRewinder implements com.bumptech.glide4110.load.data.DataRewinder<ByteBuffer> {
  private final ByteBuffer buffer;

  // Public API.
  @SuppressWarnings("WeakerAccess")
  public ByteBufferRewinder(ByteBuffer buffer) {
    this.buffer = buffer;
  }

  @NonNull
  @Override
  public ByteBuffer rewindAndGet() {
    buffer.position(0);
    return buffer;
  }

  @Override
  public void cleanup() {
    // Do nothing.
  }

  /** Factory for {@link ByteBufferRewinder}. */
  public static class Factory implements com.bumptech.glide4110.load.data.DataRewinder.Factory<ByteBuffer> {

    @NonNull
    @Override
    public DataRewinder<ByteBuffer> build(ByteBuffer data) {
      return new ByteBufferRewinder(data);
    }

    @NonNull
    @Override
    public Class<ByteBuffer> getDataClass() {
      return ByteBuffer.class;
    }
  }
}
