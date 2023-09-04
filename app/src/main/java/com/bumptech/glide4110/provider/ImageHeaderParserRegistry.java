package com.bumptech.glide4110.provider;

import androidx.annotation.NonNull;
import com.bumptech.glide4110.load.ImageHeaderParser;
import java.util.ArrayList;
import java.util.List;

/** Contains an unordered list of {@link ImageHeaderParser}s capable of parsing image headers. */
public final class ImageHeaderParserRegistry {
  private final List<ImageHeaderParser> parsers = new ArrayList<>();

  @NonNull
  public synchronized List<ImageHeaderParser> getParsers() {
    return parsers;
  }

  public synchronized void add(@NonNull ImageHeaderParser parser) {
    parsers.add(parser);
  }
}
