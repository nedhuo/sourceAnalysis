package com.bumptech.glide4110.load.engine;

import com.bumptech.glide4110.load.Encoder;
import com.bumptech.glide4110.load.Key;
import com.bumptech.glide4110.load.Options;
import com.bumptech.glide4110.load.ResourceEncoder;
import com.bumptech.glide4110.load.Transformation;
import com.bumptech.glide4110.load.model.ModelLoader;
import com.bumptech.glide4110.load.resource.UnitTransformation;
import com.bumptech.glide4110.GlideContext;
import com.bumptech.glide4110.Priority;
import com.bumptech.glide4110.Registry;
import com.bumptech.glide4110.load.engine.bitmap_recycle.ArrayPool;
import com.bumptech.glide4110.load.engine.cache.DiskCache;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

final class DecodeHelper<Transcode> {

  private final List<ModelLoader.LoadData<?>> loadData = new ArrayList<>();
  private final List<Key> cacheKeys = new ArrayList<>();

  private com.bumptech.glide4110.GlideContext glideContext;
  private Object model;
  private int width;
  private int height;
  private Class<?> resourceClass;
  private DecodeJob.DiskCacheProvider diskCacheProvider;
  private Options options;
  private Map<Class<?>, Transformation<?>> transformations;
  private Class<Transcode> transcodeClass;
  private boolean isLoadDataSet;
  private boolean isCacheKeysSet;
  private Key signature;
  private com.bumptech.glide4110.Priority priority;
  private com.bumptech.glide4110.load.engine.DiskCacheStrategy diskCacheStrategy;
  private boolean isTransformationRequired;
  private boolean isScaleOnlyOrNoTransform;

  @SuppressWarnings("unchecked")
  <R> void init(
      GlideContext glideContext,
      Object model,
      Key signature,
      int width,
      int height,
      com.bumptech.glide4110.load.engine.DiskCacheStrategy diskCacheStrategy,
      Class<?> resourceClass,
      Class<R> transcodeClass,
      com.bumptech.glide4110.Priority priority,
      Options options,
      Map<Class<?>, Transformation<?>> transformations,
      boolean isTransformationRequired,
      boolean isScaleOnlyOrNoTransform,
      DecodeJob.DiskCacheProvider diskCacheProvider) {
    this.glideContext = glideContext;
    this.model = model;
    this.signature = signature;
    this.width = width;
    this.height = height;
    this.diskCacheStrategy = diskCacheStrategy;
    this.resourceClass = resourceClass;
    this.diskCacheProvider = diskCacheProvider;
    this.transcodeClass = (Class<Transcode>) transcodeClass;
    this.priority = priority;
    this.options = options;
    this.transformations = transformations;
    this.isTransformationRequired = isTransformationRequired;
    this.isScaleOnlyOrNoTransform = isScaleOnlyOrNoTransform;
  }

  void clear() {
    glideContext = null;
    model = null;
    signature = null;
    resourceClass = null;
    transcodeClass = null;
    options = null;
    priority = null;
    transformations = null;
    diskCacheStrategy = null;

    loadData.clear();
    isLoadDataSet = false;
    cacheKeys.clear();
    isCacheKeysSet = false;
  }

  DiskCache getDiskCache() {
    return diskCacheProvider.getDiskCache();
  }

  DiskCacheStrategy getDiskCacheStrategy() {
    return diskCacheStrategy;
  }

  Priority getPriority() {
    return priority;
  }

  Options getOptions() {
    return options;
  }

  Key getSignature() {
    return signature;
  }

  int getWidth() {
    return width;
  }

  int getHeight() {
    return height;
  }

  ArrayPool getArrayPool() {
    return glideContext.getArrayPool();
  }

  Class<?> getTranscodeClass() {
    return transcodeClass;
  }

  Class<?> getModelClass() {
    return model.getClass();
  }

  List<Class<?>> getRegisteredResourceClasses() {
    return glideContext
        .getRegistry()
        .getRegisteredResourceClasses(model.getClass(), resourceClass, transcodeClass);
  }

  boolean hasLoadPath(Class<?> dataClass) {
    return getLoadPath(dataClass) != null;
  }

  <Data> LoadPath<Data, ?, Transcode> getLoadPath(Class<Data> dataClass) {
    return glideContext.getRegistry().getLoadPath(dataClass, resourceClass, transcodeClass);
  }

  boolean isScaleOnlyOrNoTransform() {
    return isScaleOnlyOrNoTransform;
  }

  @SuppressWarnings("unchecked")
  <Z> Transformation<Z> getTransformation(Class<Z> resourceClass) {
    Transformation<Z> result = (Transformation<Z>) transformations.get(resourceClass);
    if (result == null) {
      for (Entry<Class<?>, Transformation<?>> entry : transformations.entrySet()) {
        if (entry.getKey().isAssignableFrom(resourceClass)) {
          result = (Transformation<Z>) entry.getValue();
          break;
        }
      }
    }

    if (result == null) {
      if (transformations.isEmpty() && isTransformationRequired) {
        throw new IllegalArgumentException(
            "Missing transformation for "
                + resourceClass
                + ". If you wish to"
                + " ignore unknown resource types, use the optional transformation methods.");
      } else {
        return UnitTransformation.get();
      }
    }
    return result;
  }

  boolean isResourceEncoderAvailable(Resource<?> resource) {
    return glideContext.getRegistry().isResourceEncoderAvailable(resource);
  }

  <Z> ResourceEncoder<Z> getResultEncoder(Resource<Z> resource) {
    return glideContext.getRegistry().getResultEncoder(resource);
  }

  List<ModelLoader<File, ?>> getModelLoaders(File file)
      throws com.bumptech.glide4110.Registry.NoModelLoaderAvailableException {
    return glideContext.getRegistry().getModelLoaders(file);
  }

  boolean isSourceKey(Key key) {
    List<ModelLoader.LoadData<?>> loadData = getLoadData();
    //noinspection ForLoopReplaceableByForEach to improve perf
    for (int i = 0, size = loadData.size(); i < size; i++) {
      ModelLoader.LoadData<?> current = loadData.get(i);
      if (current.sourceKey.equals(key)) {
        return true;
      }
    }
    return false;
  }

  /**
   * 这个方法是有点复杂的
   * @return
   */
  List<ModelLoader.LoadData<?>> getLoadData() {
    if (!isLoadDataSet) {
      isLoadDataSet = true;
      loadData.clear();
      //1. 在Glide的构造方法最后一步就是创建了GlideContext registry也是在Glide的构造方法中构建的 append()方法添加了许多类型
      //
      List<ModelLoader<Object, ?>> modelLoaders = glideContext.getRegistry().getModelLoaders(model);
      //noinspection ForLoopReplaceableByForEach to improve perf
      for (int i = 0, size = modelLoaders.size(); i < size; i++) {
        ModelLoader<Object, ?> modelLoader = modelLoaders.get(i);
        //2. 如果图片加载使用String链接类型的URL 那么就会使用LoadData<>(url, new HttpUrlFetcher(url, timeout))（至于String是如何与HttpUrlGlideUrl映射的）
        ModelLoader.LoadData<?> current = modelLoader.buildLoadData(model, width, height, options);
        if (current != null) {
          loadData.add(current);
        }
      }
    }
    return loadData;
  }

  List<Key> getCacheKeys() {
    if (!isCacheKeysSet) {
      isCacheKeysSet = true;
      cacheKeys.clear();
      List<ModelLoader.LoadData<?>> loadData = getLoadData();
      //noinspection ForLoopReplaceableByForEach to improve perf
      for (int i = 0, size = loadData.size(); i < size; i++) {
        ModelLoader.LoadData<?> data = loadData.get(i);
        if (!cacheKeys.contains(data.sourceKey)) {
          cacheKeys.add(data.sourceKey);
        }
        for (int j = 0; j < data.alternateKeys.size(); j++) {
          if (!cacheKeys.contains(data.alternateKeys.get(j))) {
            cacheKeys.add(data.alternateKeys.get(j));
          }
        }
      }
    }
    return cacheKeys;
  }

  <X> Encoder<X> getSourceEncoder(X data) throws Registry.NoSourceEncoderAvailableException {
    return glideContext.getRegistry().getSourceEncoder(data);
  }
}
