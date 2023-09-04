package com.bumptech.glide4110.load.resource.gif;

import android.util.Log;
import androidx.annotation.NonNull;
import com.bumptech.glide4110.load.ImageHeaderParser;
import com.bumptech.glide4110.load.ImageHeaderParser.ImageType;
import com.bumptech.glide4110.load.ImageHeaderParserUtils;
import com.bumptech.glide4110.load.Options;
import com.bumptech.glide4110.load.ResourceDecoder;
import com.bumptech.glide4110.load.engine.Resource;
import com.bumptech.glide4110.load.engine.bitmap_recycle.ArrayPool;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * A relatively inefficient decoder for {@link com.bumptech.glide4110.load.resource.gif.GifDrawable}
 * that converts {@link InputStream}s to {@link ByteBuffer}s and then passes the
 * buffer to a wrapped decoder.
 */
public class StreamGifDecoder implements ResourceDecoder<InputStream, com.bumptech.glide4110.load.resource.gif.GifDrawable> {
  private static final String TAG = "StreamGifDecoder";

  private final List<ImageHeaderParser> parsers;
  private final ResourceDecoder<ByteBuffer, com.bumptech.glide4110.load.resource.gif.GifDrawable> byteBufferDecoder;
  private final com.bumptech.glide4110.load.engine.bitmap_recycle.ArrayPool byteArrayPool;

  public StreamGifDecoder(
      List<ImageHeaderParser> parsers,
      ResourceDecoder<ByteBuffer, com.bumptech.glide4110.load.resource.gif.GifDrawable> byteBufferDecoder,
      ArrayPool byteArrayPool) {
    this.parsers = parsers;
    this.byteBufferDecoder = byteBufferDecoder;
    this.byteArrayPool = byteArrayPool;
  }

  @Override
  public boolean handles(@NonNull InputStream source, @NonNull Options options) throws IOException {
    return !options.get(GifOptions.DISABLE_ANIMATION)
        && ImageHeaderParserUtils.getType(parsers, source, byteArrayPool) == ImageType.GIF;
  }

  @Override
  public Resource<GifDrawable> decode(
      @NonNull InputStream source, int width, int height, @NonNull Options options)
      throws IOException {
    byte[] data = inputStreamToBytes(source);
    if (data == null) {
      return null;
    }
    ByteBuffer byteBuffer = ByteBuffer.wrap(data);
    return byteBufferDecoder.decode(byteBuffer, width, height, options);
  }

  private static byte[] inputStreamToBytes(InputStream is) {
    final int bufferSize = 16384;
    ByteArrayOutputStream buffer = new ByteArrayOutputStream(bufferSize);
    try {
      int nRead;
      byte[] data = new byte[bufferSize];
      while ((nRead = is.read(data)) != -1) {
        buffer.write(data, 0, nRead);
      }
      buffer.flush();
    } catch (IOException e) {
      if (Log.isLoggable(TAG, Log.WARN)) {
        Log.w(TAG, "Error reading data from stream", e);
      }
      return null;
    }
    return buffer.toByteArray();
  }
}
