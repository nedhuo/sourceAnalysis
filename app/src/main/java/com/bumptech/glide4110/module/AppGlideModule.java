package com.bumptech.glide4110.module;

import android.content.Context;
import androidx.annotation.NonNull;
import com.bumptech.glide4110.GlideBuilder;

/**
 * Defines a set of dependencies and options to use when initializing Glide within an application.
 *
 * <p>There can be at most one {@link AppGlideModule} in an application. Only Applications can
 * include a {@link AppGlideModule}. Libraries must use {@link com.bumptech.glide4110.module.LibraryGlideModule}.
 *
 * <p>Classes that extend {@link AppGlideModule} must be annotated with {@link
 * com.bumptech.glide.annotation.GlideModule} to be processed correctly.
 *
 * <p>Classes that extend {@link AppGlideModule} can optionally be annotated with {@link
 * com.bumptech.glide.annotation.Excludes} to optionally exclude one or more {@link
 * com.bumptech.glide4110.module.LibraryGlideModule} and/or {@link com.bumptech.glide4110.module.GlideModule} classes.
 *
 * <p>Once an application has migrated itself and all libraries it depends on to use Glide's
 * annotation processor, {@link AppGlideModule} implementations should override {@link
 * #isManifestParsingEnabled()} and return {@code false}.
 */
// Used only in javadoc.
@SuppressWarnings("deprecation")
public abstract class AppGlideModule extends LibraryGlideModule implements com.bumptech.glide4110.module.AppliesOptions {
  /**
   * Returns {@code true} if Glide should check the AndroidManifest for {@link GlideModule}s.
   *
   * <p>Implementations should return {@code false} after they and their dependencies have migrated
   * to Glide's annotation processor.
   *
   * <p>Returns {@code true} by default.
   */
  public boolean isManifestParsingEnabled() {
    return true;
  }

  @Override
  public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
    // Default empty impl.
  }
}
