package com.bumptech.glide4110;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pools.Pool;

import com.bumptech.glide4110.load.Encoder;
import com.bumptech.glide4110.load.ImageHeaderParser;
import com.bumptech.glide4110.load.Options;
import com.bumptech.glide4110.load.ResourceDecoder;
import com.bumptech.glide4110.load.ResourceEncoder;
import com.bumptech.glide4110.load.engine.DecodePath;
import com.bumptech.glide4110.load.engine.LoadPath;
import com.bumptech.glide4110.load.engine.Resource;
import com.bumptech.glide4110.load.model.ModelLoader;
import com.bumptech.glide4110.load.model.ModelLoaderFactory;
import com.bumptech.glide4110.load.model.ModelLoaderRegistry;
import com.bumptech.glide4110.load.resource.gif.GifDrawable;
import com.bumptech.glide4110.load.resource.transcode.ResourceTranscoder;
import com.bumptech.glide4110.load.resource.transcode.TranscoderRegistry;
import com.bumptech.glide4110.util.pool.FactoryPools;
import com.bumptech.glide4110.load.data.DataRewinder;
import com.bumptech.glide4110.load.data.DataRewinderRegistry;
import com.bumptech.glide4110.provider.EncoderRegistry;
import com.bumptech.glide4110.provider.ImageHeaderParserRegistry;
import com.bumptech.glide4110.provider.LoadPathCache;
import com.bumptech.glide4110.provider.ModelToResourceClassCache;
import com.bumptech.glide4110.provider.ResourceDecoderRegistry;
import com.bumptech.glide4110.provider.ResourceEncoderRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Manages component registration to extend or replace Glide's default loading, decoding, and
 * encoding logic.
 */
// Public API.
@SuppressWarnings({"WeakerAccess", "unused"})
public class Registry {
    public static final String BUCKET_GIF = "Gif";
    public static final String BUCKET_BITMAP = "Bitmap";
    public static final String BUCKET_BITMAP_DRAWABLE = "BitmapDrawable";
    private static final String BUCKET_PREPEND_ALL = "legacy_prepend_all";
    private static final String BUCKET_APPEND_ALL = "legacy_append";

    private final ModelLoaderRegistry modelLoaderRegistry;
    private final com.bumptech.glide4110.provider.EncoderRegistry encoderRegistry;
    private final com.bumptech.glide4110.provider.ResourceDecoderRegistry decoderRegistry;
    private final com.bumptech.glide4110.provider.ResourceEncoderRegistry resourceEncoderRegistry;
    private final com.bumptech.glide4110.load.data.DataRewinderRegistry dataRewinderRegistry;
    private final TranscoderRegistry transcoderRegistry;
    private final com.bumptech.glide4110.provider.ImageHeaderParserRegistry imageHeaderParserRegistry;

    private final com.bumptech.glide4110.provider.ModelToResourceClassCache modelToResourceClassCache =
            new ModelToResourceClassCache();
    private final com.bumptech.glide4110.provider.LoadPathCache loadPathCache = new LoadPathCache();
    private final Pool<List<Throwable>> throwableListPool = FactoryPools.threadSafeList();

    public Registry() {
        this.modelLoaderRegistry = new ModelLoaderRegistry(throwableListPool);
        this.encoderRegistry = new EncoderRegistry();
        this.decoderRegistry = new ResourceDecoderRegistry();
        this.resourceEncoderRegistry = new ResourceEncoderRegistry();
        this.dataRewinderRegistry = new DataRewinderRegistry();
        this.transcoderRegistry = new TranscoderRegistry();
        this.imageHeaderParserRegistry = new ImageHeaderParserRegistry();
        setResourceDecoderBucketPriorityList(
                Arrays.asList(BUCKET_GIF, BUCKET_BITMAP, BUCKET_BITMAP_DRAWABLE));
    }

    /**
     * Registers the given {@link Encoder} for the given data class (InputStream, FileDescriptor etc).
     *
     * <p>The {@link Encoder} will be used both for the exact data class and any subtypes. For
     * example, registering an {@link Encoder} for {@link java.io.InputStream} will result in the
     * {@link Encoder} being used for {@link
     * android.content.res.AssetFileDescriptor.AutoCloseInputStream}, {@link java.io.FileInputStream}
     * and any other subclass.
     *
     * <p>If multiple {@link Encoder}s are registered for the same type or super type, the {@link
     * Encoder} that is registered first will be used.
     *
     * @deprecated Use the equivalent {@link #append(Class, Class, ModelLoaderFactory)} method
     * instead.
     */
    @NonNull
    @Deprecated
    public <Data> Registry register(@NonNull Class<Data> dataClass, @NonNull Encoder<Data> encoder) {
        return append(dataClass, encoder);
    }

    /**
     * Appends the given {@link Encoder} onto the list of available {@link Encoder}s so that it is
     * attempted after all earlier and default {@link Encoder}s for the given data class.
     *
     * <p>The {@link Encoder} will be used both for the exact data class and any subtypes. For
     * example, registering an {@link Encoder} for {@link java.io.InputStream} will result in the
     * {@link Encoder} being used for {@link
     * android.content.res.AssetFileDescriptor.AutoCloseInputStream}, {@link java.io.FileInputStream}
     * and any other subclass.
     *
     * <p>If multiple {@link Encoder}s are registered for the same type or super type, the {@link
     * Encoder} that is registered first will be used.
     *
     * @see #prepend(Class, Encoder)
     */
    @NonNull
    public <Data> Registry append(@NonNull Class<Data> dataClass, @NonNull Encoder<Data> encoder) {
        encoderRegistry.append(dataClass, encoder);
        return this;
    }

    /**
     * Prepends the given {@link Encoder} into the list of available {@link Encoder}s so that it is
     * attempted before all later and default {@link Encoder}s for the given data class.
     *
     * <p>This method allows you to replace the default {@link Encoder} because it ensures the
     * registered {@link Encoder} will run first. If multiple {@link Encoder}s are registered for the
     * same type or super type, the {@link Encoder} that is registered first will be used.
     *
     * @see #append(Class, Encoder)
     */
    @NonNull
    public <Data> Registry prepend(@NonNull Class<Data> dataClass, @NonNull Encoder<Data> encoder) {
        encoderRegistry.prepend(dataClass, encoder);
        return this;
    }

    /**
     * Appends the given {@link ResourceDecoder} onto the list of all available {@link
     * ResourceDecoder}s allowing it to be used if all earlier and default {@link ResourceDecoder}s
     * for the given types fail (or there are none).
     *
     * <p>If you're attempting to replace an existing {@link ResourceDecoder} or would like to ensure
     * that your {@link ResourceDecoder} gets the chance to run before an existing {@link
     * ResourceDecoder}, use {@link #prepend(Class, Class, ResourceDecoder)}. This method is best for
     * new types of resources and data or as a way to add an additional fallback decoder for an
     * existing type of data.
     *
     * @param dataClass     The data that will be decoded from ({@link java.io.InputStream}, {@link
     *                      java.io.FileDescriptor} etc).
     * @param resourceClass The resource that will be decoded to ({@link android.graphics.Bitmap},
     *                      {@link GifDrawable} etc).
     * @param decoder       The {@link ResourceDecoder} to register.
     * @see #append(String, Class, Class, ResourceDecoder)
     * @see #prepend(Class, Class, ResourceDecoder)
     */
    @NonNull
    public <Data, TResource> Registry append(
            @NonNull Class<Data> dataClass,
            @NonNull Class<TResource> resourceClass,
            @NonNull ResourceDecoder<Data, TResource> decoder) {
        append(BUCKET_APPEND_ALL, dataClass, resourceClass, decoder);
        return this;
    }


    /**
     * Appends the given {@link ResourceDecoder} onto the list of available {@link ResourceDecoder}s
     * in this bucket, allowing it to be used if all earlier and default {@link ResourceDecoder}s for
     * the given types in this bucket fail (or there are none).
     *
     * <p>If you're attempting to replace an existing {@link ResourceDecoder} or would like to ensure
     * that your {@link ResourceDecoder} gets the chance to run before an existing {@link
     * ResourceDecoder}, use {@link #prepend(Class, Class, ResourceDecoder)}. This method is best for
     * new types of resources and data or as a way to add an additional fallback decoder for an
     * existing type of data.
     *
     * @param bucket        The bucket identifier to add this decoder to.
     * @param dataClass     The data that will be decoded from ({@link java.io.InputStream}, {@link
     *                      java.io.FileDescriptor} etc).
     * @param resourceClass The resource that will be decoded to ({@link android.graphics.Bitmap},
     *                      {@link GifDrawable} etc).
     * @param decoder       The {@link ResourceDecoder} to register.
     * @see #prepend(String, Class, Class, ResourceDecoder)
     * @see #setResourceDecoderBucketPriorityList(List)
     * <p>
     * 将给定的{@link ResourceDecoder}附加到列表中 允许它在所有先前的和默认的{@link ResourceDecoder}失败(或者没有)后使用。
     * <p>
     * 如果你试图替换现有的{@link ResourceDecoder}，或者想确保你的{@link ResourceDecoder}有机会在现有的{@link ResourceDecoder}之前运行，请使用{@link #prepend(Class, Class, ResourceDecoder)}。这种方法最适合
     * 新类型的资源和数据，或者作为为现有类型的数据添加额外的后备解码器的一种方式。
     */
    @NonNull
    public <Data, TResource> Registry append(
            @NonNull String bucket,
            @NonNull Class<Data> dataClass,
            @NonNull Class<TResource> resourceClass,
            @NonNull ResourceDecoder<Data, TResource> decoder) {
        decoderRegistry.append(bucket, decoder, dataClass, resourceClass);
        return this;
    }

    /**
     * Prepends the given {@link ResourceDecoder} into the list of all available {@link
     * ResourceDecoder}s so that it is attempted before all later and default {@link ResourceDecoder}s
     * for the given types.
     *
     * <p>This method allows you to replace the default {@link ResourceDecoder} because it ensures the
     * registered {@link ResourceDecoder} will run first. You can use the {@link
     * ResourceDecoder#handles(Object, Options)} to fall back to the default {@link ResourceDecoder}s
     * if you only want to change the default functionality for certain types of data.
     *
     * @param dataClass     The data that will be decoded from ({@link java.io.InputStream}, {@link
     *                      java.io.FileDescriptor} etc).
     * @param resourceClass The resource that will be decoded to ({@link android.graphics.Bitmap},
     *                      {@link GifDrawable} etc).
     * @param decoder       The {@link ResourceDecoder} to register.
     * @see #prepend(String, Class, Class, ResourceDecoder)
     * @see #append(Class, Class, ResourceDecoder)
     */
    @NonNull
    public <Data, TResource> Registry prepend(
            @NonNull Class<Data> dataClass,
            @NonNull Class<TResource> resourceClass,
            @NonNull ResourceDecoder<Data, TResource> decoder) {
        prepend(BUCKET_PREPEND_ALL, dataClass, resourceClass, decoder);
        return this;
    }

    /**
     * Prepends the given {@link ResourceDecoder} into the list of available {@link ResourceDecoder}s
     * in the same bucket so that it is attempted before all later and default {@link
     * ResourceDecoder}s for the given types in that bucket.
     *
     * <p>This method allows you to replace the default {@link ResourceDecoder} for this bucket
     * because it ensures the registered {@link ResourceDecoder} will run first. You can use the
     * {@link ResourceDecoder#handles(Object, Options)} to fall back to the default {@link
     * ResourceDecoder}s if you only want to change the default functionality for certain types of
     * data.
     *
     * @param bucket        The bucket identifier to add this decoder to.
     * @param dataClass     The data that will be decoded from ({@link java.io.InputStream}, {@link
     *                      java.io.FileDescriptor} etc).
     * @param resourceClass The resource that will be decoded to ({@link android.graphics.Bitmap},
     *                      {@link GifDrawable} etc).
     * @param decoder       The {@link ResourceDecoder} to register.
     * @see #append(String, Class, Class, ResourceDecoder)
     * @see #setResourceDecoderBucketPriorityList(List)
     */
    @NonNull
    public <Data, TResource> Registry prepend(
            @NonNull String bucket,
            @NonNull Class<Data> dataClass,
            @NonNull Class<TResource> resourceClass,
            @NonNull ResourceDecoder<Data, TResource> decoder) {
        decoderRegistry.prepend(bucket, decoder, dataClass, resourceClass);
        return this;
    }

    /**
     * Overrides the default ordering of resource decoder buckets. You may also add custom buckets
     * which are identified as a unique string. Glide will attempt to decode using decoders in the
     * highest priority bucket before moving on to the next one.
     *
     * <p>The default order is [{@link #BUCKET_GIF}, {@link #BUCKET_BITMAP}, {@link
     * #BUCKET_BITMAP_DRAWABLE}].
     *
     * <p>When registering decoders, you can use these buckets to specify the ordering relative only
     * to other decoders in that bucket.
     *
     * @param buckets The list of bucket identifiers in order from highest priority to least priority.
     * @see #append(String, Class, Class, ResourceDecoder)
     * @see #prepend(String, Class, Class, ResourceDecoder)
     */
    // Final to avoid a PMD error.
    @NonNull
    public final Registry setResourceDecoderBucketPriorityList(@NonNull List<String> buckets) {
        // See #3296 and https://bugs.openjdk.java.net/browse/JDK-6260652.
        List<String> modifiedBuckets = new ArrayList<>(buckets.size());
        modifiedBuckets.addAll(buckets);
        modifiedBuckets.add(0, BUCKET_PREPEND_ALL);
        modifiedBuckets.add(BUCKET_APPEND_ALL);
        decoderRegistry.setBucketPriorityList(modifiedBuckets);
        return this;
    }

    /**
     * Appends the given {@link ResourceEncoder} into the list of available {@link ResourceEncoder}s
     * so that it is attempted after all earlier and default {@link ResourceEncoder}s for the given
     * data type.
     *
     * <p>The {@link ResourceEncoder} will be used both for the exact resource class and any subtypes.
     * For example, registering an {@link ResourceEncoder} for {@link
     * android.graphics.drawable.Drawable} (not recommended) will result in the {@link
     * ResourceEncoder} being used for {@link android.graphics.drawable.BitmapDrawable} and {@link
     * GifDrawable} and any other subclass.
     *
     * <p>If multiple {@link ResourceEncoder}s are registered for the same type or super type, the
     * {@link ResourceEncoder} that is registered first will be used.
     *
     * @deprecated Use the equivalent {@link #append(Class, ResourceEncoder)} method instead.
     */
    @NonNull
    @Deprecated
    public <TResource> Registry register(
            @NonNull Class<TResource> resourceClass, @NonNull ResourceEncoder<TResource> encoder) {
        return append(resourceClass, encoder);
    }

    /**
     * Appends the given {@link ResourceEncoder} into the list of available {@link ResourceEncoder}s
     * so that it is attempted after all earlier and default {@link ResourceEncoder}s for the given
     * data type.
     *
     * <p>The {@link ResourceEncoder} will be used both for the exact resource class and any subtypes.
     * For example, registering an {@link ResourceEncoder} for {@link
     * android.graphics.drawable.Drawable} (not recommended) will result in the {@link
     * ResourceEncoder} being used for {@link android.graphics.drawable.BitmapDrawable} and {@link
     * GifDrawable} and any other subclass.
     *
     * <p>If multiple {@link ResourceEncoder}s are registered for the same type or super type, the
     * {@link ResourceEncoder} that is registered first will be used.
     *
     * @see #prepend(Class, ResourceEncoder)
     */
    @NonNull
    public <TResource> Registry append(
            @NonNull Class<TResource> resourceClass, @NonNull ResourceEncoder<TResource> encoder) {
        resourceEncoderRegistry.append(resourceClass, encoder);
        return this;
    }

    /**
     * Prepends the given {@link ResourceEncoder} into the list of available {@link ResourceEncoder}s
     * so that it is attempted before all later and default {@link ResourceEncoder}s for the given
     * data type.
     *
     * <p>This method allows you to replace the default {@link ResourceEncoder} because it ensures the
     * registered {@link ResourceEncoder} will run first. If multiple {@link ResourceEncoder}s are
     * registered for the same type or super type, the {@link ResourceEncoder} that is registered
     * first will be used.
     *
     * @see #append(Class, ResourceEncoder)
     */
    @NonNull
    public <TResource> Registry prepend(
            @NonNull Class<TResource> resourceClass, @NonNull ResourceEncoder<TResource> encoder) {
        resourceEncoderRegistry.prepend(resourceClass, encoder);
        return this;
    }

    /**
     * Registers a new {@link com.bumptech.glide4110.load.data.DataRewinder.Factory} to handle a
     * non-default data type that can be rewind to allow for efficient reads of file headers.
     */
    @NonNull
    public Registry register(@NonNull com.bumptech.glide4110.load.data.DataRewinder.Factory<?> factory) {
        dataRewinderRegistry.register(factory);
        return this;
    }

    /**
     * Registers the given {@link ResourceTranscoder} to convert from the given resource {@link Class}
     * to the given transcode {@link Class}.
     *
     * @param resourceClass  The class that will be transcoded from (e.g. {@link
     *                       android.graphics.Bitmap}).
     * @param transcodeClass The class that will be transcoded to (e.g. {@link
     *                       android.graphics.drawable.BitmapDrawable}).
     * @param transcoder     The {@link ResourceTranscoder} to register.
     */
    @NonNull
    public <TResource, Transcode> Registry register(
            @NonNull Class<TResource> resourceClass,
            @NonNull Class<Transcode> transcodeClass,
            @NonNull ResourceTranscoder<TResource, Transcode> transcoder) {
        transcoderRegistry.register(resourceClass, transcodeClass, transcoder);
        return this;
    }

    /**
     * Registers a new {@link ImageHeaderParser} that can obtain some basic metadata from an image
     * header (orientation, type etc).
     */
    @NonNull
    public Registry register(@NonNull ImageHeaderParser parser) {
        imageHeaderParserRegistry.add(parser);
        return this;
    }

    /**
     * Appends a new {@link ModelLoaderFactory} onto the end of the existing set so that the
     * constructed {@link ModelLoader} will be tried after all default and previously registered
     * {@link ModelLoader}s for the given model and data classes.
     *
     * <p>If you're attempting to replace an existing {@link ModelLoader}, use {@link #prepend(Class,
     * Class, ModelLoaderFactory)}. This method is best for new types of models and/or data or as a
     * way to add an additional fallback loader for an existing type of model/data.
     *
     * <p>If multiple {@link ModelLoaderFactory}s are registered for the same model and/or data
     * classes, the {@link ModelLoader}s they produce will be attempted in the order the {@link
     * ModelLoaderFactory}s were registered. Only if all {@link ModelLoader}s fail will the entire
     * request fail.
     *
     * @param modelClass The model class (e.g. URL, file path).
     * @param dataClass  the data class (e.g. {@link java.io.InputStream}, {@link
     *                   java.io.FileDescriptor}).
     * @see #prepend(Class, Class, ModelLoaderFactory)
     * @see #replace(Class, Class, ModelLoaderFactory)
     */
    @NonNull
    public <Model, Data> Registry append(
            @NonNull Class<Model> modelClass,
            @NonNull Class<Data> dataClass,
            @NonNull ModelLoaderFactory<Model, Data> factory) {
        modelLoaderRegistry.append(modelClass, dataClass, factory);
        return this;
    }

    /**
     * Prepends a new {@link ModelLoaderFactory} onto the beginning of the existing set so that the
     * constructed {@link ModelLoader} will be tried before all default and previously registered
     * {@link ModelLoader}s for the given model and data classes.
     *
     * <p>If you're attempting to add additional functionality or add a backup that should run only
     * after the default {@link ModelLoader}s run, use {@link #append(Class, Class,
     * ModelLoaderFactory)}. This method is best for adding an additional case to Glide's existing
     * functionality that should run first. This method will still run Glide's default {@link
     * ModelLoader}s if the prepended {@link ModelLoader}s fail.
     *
     * <p>If multiple {@link ModelLoaderFactory}s are registered for the same model and/or data
     * classes, the {@link ModelLoader}s they produce will be attempted in the order the {@link
     * ModelLoaderFactory}s were registered. Only if all {@link ModelLoader}s fail will the entire
     * request fail.
     *
     * @param modelClass The model class (e.g. URL, file path).
     * @param dataClass  the data class (e.g. {@link java.io.InputStream}, {@link
     *                   java.io.FileDescriptor}).
     * @see #append(Class, Class, ModelLoaderFactory)
     * @see #replace(Class, Class, ModelLoaderFactory)
     */
    @NonNull
    public <Model, Data> Registry prepend(
            @NonNull Class<Model> modelClass,
            @NonNull Class<Data> dataClass,
            @NonNull ModelLoaderFactory<Model, Data> factory) {
        modelLoaderRegistry.prepend(modelClass, dataClass, factory);
        return this;
    }

    /**
     * Removes all default and previously registered {@link ModelLoaderFactory}s for the given data
     * and model class and replaces all of them with the single {@link ModelLoader} provided.
     *
     * <p>If you're attempting to add additional functionality or add a backup that should run only
     * after the default {@link ModelLoader}s run, use {@link #append(Class, Class,
     * ModelLoaderFactory)}. This method should be used only when you want to ensure that Glide's
     * default {@link ModelLoader}s do not run.
     *
     * <p>One good use case for this method is when you want to replace Glide's default networking
     * library with your OkHttp, Volley, or your own implementation. Using {@link #prepend(Class,
     * Class, ModelLoaderFactory)} or {@link #append(Class, Class, ModelLoaderFactory)} may still
     * allow Glide's default networking library to run in some cases. Using this method will ensure
     * that only your networking library will run and that the request will fail otherwise.
     *
     * @param modelClass The model class (e.g. URL, file path).
     * @param dataClass  the data class (e.g. {@link java.io.InputStream}, {@link
     *                   java.io.FileDescriptor}).
     * @see #prepend(Class, Class, ModelLoaderFactory)
     * @see #append(Class, Class, ModelLoaderFactory)
     */
    @NonNull
    public <Model, Data> Registry replace(
            @NonNull Class<Model> modelClass,
            @NonNull Class<Data> dataClass,
            @NonNull ModelLoaderFactory<? extends Model, ? extends Data> factory) {
        modelLoaderRegistry.replace(modelClass, dataClass, factory);
        return this;
    }

    @Nullable
    public <Data, TResource, Transcode> LoadPath<Data, TResource, Transcode> getLoadPath(
            @NonNull Class<Data> dataClass,
            @NonNull Class<TResource> resourceClass,
            @NonNull Class<Transcode> transcodeClass) {
        LoadPath<Data, TResource, Transcode> result =
                loadPathCache.get(dataClass, resourceClass, transcodeClass);
        if (loadPathCache.isEmptyLoadPath(result)) {
            return null;
        } else if (result == null) {
            List<DecodePath<Data, TResource, Transcode>> decodePaths =
                    getDecodePaths(dataClass, resourceClass, transcodeClass);
            // It's possible there is no way to decode or transcode to the desired types from a given
            // data class.
            if (decodePaths.isEmpty()) {
                result = null;
            } else {
                result =
                        new LoadPath<>(
                                dataClass, resourceClass, transcodeClass, decodePaths, throwableListPool);
            }
            loadPathCache.put(dataClass, resourceClass, transcodeClass, result);
        }
        return result;
    }

    @NonNull
    private <Data, TResource, Transcode> List<DecodePath<Data, TResource, Transcode>> getDecodePaths(
            @NonNull Class<Data> dataClass,
            @NonNull Class<TResource> resourceClass,
            @NonNull Class<Transcode> transcodeClass) {
        List<DecodePath<Data, TResource, Transcode>> decodePaths = new ArrayList<>();
        List<Class<TResource>> registeredResourceClasses =
                decoderRegistry.getResourceClasses(dataClass, resourceClass);

        for (Class<TResource> registeredResourceClass : registeredResourceClasses) {
            List<Class<Transcode>> registeredTranscodeClasses =
                    transcoderRegistry.getTranscodeClasses(registeredResourceClass, transcodeClass);

            for (Class<Transcode> registeredTranscodeClass : registeredTranscodeClasses) {

                List<ResourceDecoder<Data, TResource>> decoders =
                        decoderRegistry.getDecoders(dataClass, registeredResourceClass);
                ResourceTranscoder<TResource, Transcode> transcoder =
                        transcoderRegistry.get(registeredResourceClass, registeredTranscodeClass);
                @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
                DecodePath<Data, TResource, Transcode> path =
                        new DecodePath<>(
                                dataClass,
                                registeredResourceClass,
                                registeredTranscodeClass,
                                decoders,
                                transcoder,
                                throwableListPool);
                decodePaths.add(path);
            }
        }
        return decodePaths;
    }

    @NonNull
    public <Model, TResource, Transcode> List<Class<?>> getRegisteredResourceClasses(
            @NonNull Class<Model> modelClass,
            @NonNull Class<TResource> resourceClass,
            @NonNull Class<Transcode> transcodeClass) {
        List<Class<?>> result =
                modelToResourceClassCache.get(modelClass, resourceClass, transcodeClass);

        if (result == null) {
            result = new ArrayList<>();
            List<Class<?>> dataClasses = modelLoaderRegistry.getDataClasses(modelClass);
            for (Class<?> dataClass : dataClasses) {
                List<? extends Class<?>> registeredResourceClasses =
                        decoderRegistry.getResourceClasses(dataClass, resourceClass);
                for (Class<?> registeredResourceClass : registeredResourceClasses) {
                    List<Class<Transcode>> registeredTranscodeClasses =
                            transcoderRegistry.getTranscodeClasses(registeredResourceClass, transcodeClass);
                    if (!registeredTranscodeClasses.isEmpty() && !result.contains(registeredResourceClass)) {
                        result.add(registeredResourceClass);
                    }
                }
            }
            modelToResourceClassCache.put(
                    modelClass, resourceClass, transcodeClass, Collections.unmodifiableList(result));
        }

        return result;
    }

    public boolean isResourceEncoderAvailable(@NonNull Resource<?> resource) {
        return resourceEncoderRegistry.get(resource.getResourceClass()) != null;
    }

    @NonNull
    public <X> ResourceEncoder<X> getResultEncoder(@NonNull Resource<X> resource)
            throws NoResultEncoderAvailableException {
        ResourceEncoder<X> resourceEncoder = resourceEncoderRegistry.get(resource.getResourceClass());
        if (resourceEncoder != null) {
            return resourceEncoder;
        }
        throw new NoResultEncoderAvailableException(resource.getResourceClass());
    }

    @NonNull
    @SuppressWarnings("unchecked")
    public <X> Encoder<X> getSourceEncoder(@NonNull X data) throws NoSourceEncoderAvailableException {
        Encoder<X> encoder = encoderRegistry.getEncoder((Class<X>) data.getClass());
        if (encoder != null) {
            return encoder;
        }
        throw new NoSourceEncoderAvailableException(data.getClass());
    }

    @NonNull
    public <X> DataRewinder<X> getRewinder(@NonNull X data) {
        return dataRewinderRegistry.build(data);
    }

    @NonNull
    public <Model> List<ModelLoader<Model, ?>> getModelLoaders(@NonNull Model model) {
        return modelLoaderRegistry.getModelLoaders(model);
    }

    @NonNull
    public List<ImageHeaderParser> getImageHeaderParsers() {
        List<ImageHeaderParser> result = imageHeaderParserRegistry.getParsers();
        if (result.isEmpty()) {
            throw new NoImageHeaderParserException();
        }
        return result;
    }

    /**
     * Thrown when no {@link ModelLoader} is registered for a given
     * model class.
     */
    // Never serialized by Glide.
    @SuppressWarnings("serial")
    public static class NoModelLoaderAvailableException extends MissingComponentException {

        public NoModelLoaderAvailableException(@NonNull Object model) {
            super("Failed to find any ModelLoaders registered for model class: " + model.getClass());
        }

        public <M> NoModelLoaderAvailableException(
                @NonNull M model, @NonNull List<ModelLoader<M, ?>> matchingButNotHandlingModelLoaders) {
            super(
                    "Found ModelLoaders for model class: "
                            + matchingButNotHandlingModelLoaders
                            + ", but none that handle this specific model instance: "
                            + model);
        }

        public NoModelLoaderAvailableException(
                @NonNull Class<?> modelClass, @NonNull Class<?> dataClass) {
            super("Failed to find any ModelLoaders for model: " + modelClass + " and data: " + dataClass);
        }
    }

    /**
     * Thrown when no {@link ResourceEncoder} is registered for a given resource class.
     */
    // Never serialized by Glide.
    @SuppressWarnings("serial")
    public static class NoResultEncoderAvailableException extends MissingComponentException {
        public NoResultEncoderAvailableException(@NonNull Class<?> resourceClass) {
            super(
                    "Failed to find result encoder for resource class: "
                            + resourceClass
                            + ", you may need to consider registering a new Encoder for the requested type or"
                            + " DiskCacheStrategy.DATA/DiskCacheStrategy.NONE if caching your transformed"
                            + " resource is unnecessary.");
        }
    }

    /**
     * Thrown when no {@link Encoder} is registered for a given data class.
     */
    // Never serialized by Glide.
    @SuppressWarnings("serial")
    public static class NoSourceEncoderAvailableException extends MissingComponentException {
        public NoSourceEncoderAvailableException(@NonNull Class<?> dataClass) {
            super("Failed to find source encoder for data class: " + dataClass);
        }
    }

    /**
     * Thrown when some necessary component is missing for a load.
     */
    // Never serialized by Glide.
    @SuppressWarnings("serial")
    public static class MissingComponentException extends RuntimeException {
        public MissingComponentException(@NonNull String message) {
            super(message);
        }
    }

    /**
     * Thrown when no {@link ImageHeaderParser} is registered.
     */
    // Never serialized by Glide.
    @SuppressWarnings("serial")
    public static final class NoImageHeaderParserException extends MissingComponentException {
        public NoImageHeaderParserException() {
            super("Failed to find image header parser.");
        }
    }
}
