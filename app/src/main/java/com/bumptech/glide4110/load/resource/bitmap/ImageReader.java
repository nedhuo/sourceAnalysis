package com.bumptech.glide4110.load.resource.bitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import com.bumptech.glide4110.load.ImageHeaderParser;
import com.bumptech.glide4110.load.ImageHeaderParserUtils;
import com.bumptech.glide4110.load.data.DataRewinder;
import com.bumptech.glide4110.load.data.InputStreamRewinder;
import com.bumptech.glide4110.load.data.ParcelFileDescriptorRewinder;
import com.bumptech.glide4110.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide4110.util.Preconditions;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * This is a helper class for {@link Downsampler} that abstracts out image operations from the input
 * type wrapped into a {@link DataRewinder}.
 */
interface ImageReader {
  @Nullable
  Bitmap decodeBitmap(BitmapFactory.Options options) throws IOException;

  ImageHeaderParser.ImageType getImageType() throws IOException;

  int getImageOrientation() throws IOException;

  void stopGrowingBuffers();

  final class InputStreamImageReader implements ImageReader {
    private final com.bumptech.glide4110.load.data.InputStreamRewinder dataRewinder;
    private final com.bumptech.glide4110.load.engine.bitmap_recycle.ArrayPool byteArrayPool;
    private final List<ImageHeaderParser> parsers;

    InputStreamImageReader(
        InputStream is, List<ImageHeaderParser> parsers, com.bumptech.glide4110.load.engine.bitmap_recycle.ArrayPool byteArrayPool) {
      this.byteArrayPool = com.bumptech.glide4110.util.Preconditions.checkNotNull(byteArrayPool);
      this.parsers = com.bumptech.glide4110.util.Preconditions.checkNotNull(parsers);

      dataRewinder = new InputStreamRewinder(is, byteArrayPool);
    }

    @Nullable
    @Override
    public Bitmap decodeBitmap(BitmapFactory.Options options) throws IOException {
      return BitmapFactory.decodeStream(dataRewinder.rewindAndGet(), null, options);
    }

    @Override
    public ImageHeaderParser.ImageType getImageType() throws IOException {
      return ImageHeaderParserUtils.getType(parsers, dataRewinder.rewindAndGet(), byteArrayPool);
    }

    @Override
    public int getImageOrientation() throws IOException {
      return ImageHeaderParserUtils.getOrientation(
          parsers, dataRewinder.rewindAndGet(), byteArrayPool);
    }

    @Override
    public void stopGrowingBuffers() {
      dataRewinder.fixMarkLimits();
    }
  }

  @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
  final class ParcelFileDescriptorImageReader implements ImageReader {
    private final com.bumptech.glide4110.load.engine.bitmap_recycle.ArrayPool byteArrayPool;
    private final List<ImageHeaderParser> parsers;
    private final com.bumptech.glide4110.load.data.ParcelFileDescriptorRewinder dataRewinder;

    ParcelFileDescriptorImageReader(
        ParcelFileDescriptor parcelFileDescriptor,
        List<ImageHeaderParser> parsers,
        ArrayPool byteArrayPool) {
      this.byteArrayPool = com.bumptech.glide4110.util.Preconditions.checkNotNull(byteArrayPool);
      this.parsers = Preconditions.checkNotNull(parsers);

      dataRewinder = new ParcelFileDescriptorRewinder(parcelFileDescriptor);
    }

    @Nullable
    @Override
    public Bitmap decodeBitmap(BitmapFactory.Options options) throws IOException {
      return BitmapFactory.decodeFileDescriptor(
          dataRewinder.rewindAndGet().getFileDescriptor(), null, options);
    }

    @Override
    public ImageHeaderParser.ImageType getImageType() throws IOException {
      return ImageHeaderParserUtils.getType(parsers, dataRewinder, byteArrayPool);
    }

    @Override
    public int getImageOrientation() throws IOException {
      return ImageHeaderParserUtils.getOrientation(parsers, dataRewinder, byteArrayPool);
    }

    @Override
    public void stopGrowingBuffers() {
      // Nothing to do here.
    }
  }
}
