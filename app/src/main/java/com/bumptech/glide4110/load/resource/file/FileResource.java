package com.bumptech.glide4110.load.resource.file;

import com.bumptech.glide4110.load.resource.SimpleResource;
import com.bumptech.glide4110.load.engine.Resource;

import java.io.File;

/** A simple {@link Resource} that wraps a {@link File}. */
// Public API.
@SuppressWarnings("WeakerAccess")
public class FileResource extends SimpleResource<File> {
  public FileResource(File file) {
    super(file);
  }
}
