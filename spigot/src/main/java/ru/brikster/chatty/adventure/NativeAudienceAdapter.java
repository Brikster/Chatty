package ru.brikster.chatty.adventure;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public final class NativeAudienceAdapter implements Audience {

    private static final Cache<Object, Object> CONVERTED_OBJECTS_CACHE =
            CacheBuilder.newBuilder()
                    .weakKeys()
                    .expireAfterAccess(Duration.ofMinutes(5))
                    .maximumSize(1024)
                    .build();

    // concat it to avoid shadow relocate rule
    private static final String NET_KYORI_ADVENTURE = "net.".concat("kyori.adventure.");
    private static final String AUDIENCE_CLASS_NAME = NET_KYORI_ADVENTURE.concat("audience.Audience");
    private static final String COMPONENT_CLASS_NAME = NET_KYORI_ADVENTURE.concat("text.Component");
    private static final String IDENTITY_CLASS_NAME = NET_KYORI_ADVENTURE.concat("identity.Identity");
    private static final String KEY_CLASS_NAME = NET_KYORI_ADVENTURE.concat("key.Key");
    private static final String SOUND_CLASS_NAME = NET_KYORI_ADVENTURE.concat("sound.Sound");
    private static final String TITLE_CLASS_NAME = NET_KYORI_ADVENTURE.concat("title.Title");
    private static final String TITLE_TIMES_CLASS_NAME = NET_KYORI_ADVENTURE.concat("title.Title$Times");
    private static final String SOUND_SOURCE_CLASS_NAME = NET_KYORI_ADVENTURE.concat("sound.Sound$Source");
    private static final String GSON_SERIALIZER_CLASS_NAME = NET_KYORI_ADVENTURE
            .concat("text.serializer.gson.GsonComponentSerializer");
    private static final String COMPONENT_SERIALIZER_CLASS_NAME = NET_KYORI_ADVENTURE
            .concat("text.serializer.ComponentSerializer");

    /**
     * Serializer used to bridge Chatty's shaded Adventure to the server's native
     * Adventure. The two may be different versions, and the JSON schema for click
     * and hover events changed between them (camelCase {@code clickEvent} ->
     * snake_case {@code click_event}). The default {@code gson()} serializer emits
     * only the newest schema, which an older native Adventure cannot read, so it
     * silently drops every click/hover event. {@link JSONOptions#compatibility()}
     * emits both schemas, keeping the bridge lossless on any server version.
     */
    private static final GsonComponentSerializer COMPATIBLE_GSON = GsonComponentSerializer.builder()
            .options(JSONOptions.compatibility())
            .build();

    private static final Class<?> SOUND_SOURCE_CLASS;
    private static final Class<?> COMPONENT_CLASS;

    private static final Lookup LOOKUP;

    private static final MethodHandle SEND_MESSAGE_METHOD;
    private static final MethodHandle SEND_MESSAGE_WITH_IDENTITY_METHOD;
    private static final MethodHandle SEND_ACTION_BAR_METHOD;
    private static final MethodHandle PLAY_SOUND_METHOD;
    private static final MethodHandle SHOW_TITLE_METHOD;

    private static final Object GSON_COMPONENT_SERIALIZER;
    private static final MethodHandle DESERIALIZE_METHOD;
    private static final MethodHandle SERIALIZE_METHOD;
    private static final MethodHandle IDENTITY_FACTORY_METHOD;
    private static final MethodHandle KEY_FACTORY_METHOD;
    private static final MethodHandle SOUND_FACTORY_METHOD;
    private static final MethodHandle TITLE_FACTORY_METHOD;
    private static MethodHandle TITLE_TIMES_FACTORY_METHOD;

    static {
        try {
            LOOKUP = MethodHandles.lookup();
            Class<?> audienceClass = Class.forName(AUDIENCE_CLASS_NAME);
            Class<?> componentClass = Class.forName(COMPONENT_CLASS_NAME);
            COMPONENT_CLASS = componentClass;
            Class<?> identityClass = Class.forName(IDENTITY_CLASS_NAME);
            Class<?> keyClass = Class.forName(KEY_CLASS_NAME);
            Class<?> soundClass = Class.forName(SOUND_CLASS_NAME);
            Class<?> titleClass = Class.forName(TITLE_CLASS_NAME);
            Class<?> titleTimesClass = Class.forName(TITLE_TIMES_CLASS_NAME);
            SOUND_SOURCE_CLASS = Class.forName(SOUND_SOURCE_CLASS_NAME);

            SEND_MESSAGE_METHOD = LOOKUP.findVirtual(audienceClass, "sendMessage",
                    MethodType.methodType(void.class, componentClass));
            MethodHandle sendMessageWithIdentity;
            try {
                sendMessageWithIdentity = LOOKUP.findVirtual(audienceClass, "sendMessage",
                        MethodType.methodType(void.class, identityClass, componentClass));
            } catch (NoSuchMethodException e) {
                sendMessageWithIdentity = null;
            }
            SEND_MESSAGE_WITH_IDENTITY_METHOD = sendMessageWithIdentity;
            SEND_ACTION_BAR_METHOD = LOOKUP.findVirtual(audienceClass, "sendActionBar",
                    MethodType.methodType(void.class, componentClass));
            PLAY_SOUND_METHOD = LOOKUP.findVirtual(audienceClass, "playSound",
                    MethodType.methodType(void.class, soundClass));
            SHOW_TITLE_METHOD = LOOKUP.findVirtual(audienceClass, "showTitle",
                    MethodType.methodType(void.class, titleClass));

            Class<?> gsonComponentSerializerClass = Class.forName(GSON_SERIALIZER_CLASS_NAME);
            GSON_COMPONENT_SERIALIZER = gsonComponentSerializerClass.getMethod("gson").invoke(null);
            Class<?> componentSerializerClass = Class.forName(COMPONENT_SERIALIZER_CLASS_NAME);
            DESERIALIZE_METHOD = LOOKUP.findVirtual(componentSerializerClass, "deserialize",
                    MethodType.methodType(componentClass, Object.class));
            SERIALIZE_METHOD = LOOKUP.findVirtual(componentSerializerClass, "serialize",
                    MethodType.methodType(Object.class, componentClass));
            IDENTITY_FACTORY_METHOD = LOOKUP.findStatic(identityClass, "identity",
                    MethodType.methodType(identityClass, UUID.class));

            KEY_FACTORY_METHOD = LOOKUP.findStatic(keyClass, "key",
                    MethodType.methodType(keyClass, String.class, String.class));
            SOUND_FACTORY_METHOD = LOOKUP.findStatic(soundClass, "sound",
                    MethodType.methodType(soundClass, keyClass, SOUND_SOURCE_CLASS, float.class, float.class));
            TITLE_FACTORY_METHOD = LOOKUP.findStatic(titleClass, "title",
                    MethodType.methodType(titleClass, componentClass, componentClass, titleTimesClass));
            try {
                TITLE_TIMES_FACTORY_METHOD = LOOKUP.findStatic(titleTimesClass, "times",
                        MethodType.methodType(titleTimesClass, Duration.class, Duration.class, Duration.class));
            } catch (NoSuchMethodException m) {
                TITLE_TIMES_FACTORY_METHOD = LOOKUP.findStatic(titleTimesClass, "of",
                        MethodType.methodType(titleTimesClass, Duration.class, Duration.class, Duration.class));
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    private final Object target;

    public NativeAudienceAdapter(Object target) {
        this.target = target;
    }

    @SneakyThrows
    @Override
    public void sendMessage(@NotNull Component message) {
        Object convertedComponent = getCachedOrConvert(message, () -> convertComponent(message));
        SEND_MESSAGE_METHOD.invoke(target, convertedComponent);
    }

    @SneakyThrows
    @SuppressWarnings("deprecation")
    @Override
    public void sendMessage(@NotNull Identity source, @NotNull Component message) {
        if (SEND_MESSAGE_WITH_IDENTITY_METHOD == null) {
            sendMessage(message);
            return;
        }
        Object identity = getCachedOrConvert(source, () -> IDENTITY_FACTORY_METHOD.invoke(source.uuid()));
        Object convertedComponent = getCachedOrConvert(message, () -> convertComponent(message));
        SEND_MESSAGE_WITH_IDENTITY_METHOD.invoke(target, identity, convertedComponent);
    }

    @SneakyThrows
    @Override
    public void playSound(@NotNull Sound sound) {
        Object convertedSound = getCachedOrConvert(sound, () -> convertSound(sound));
        PLAY_SOUND_METHOD.invoke(target, convertedSound);
    }

    @SneakyThrows
    @Override
    public void sendActionBar(@NotNull Component message) {
        Object convertedComponent = getCachedOrConvert(message, () -> convertComponent(message));
        SEND_ACTION_BAR_METHOD.invoke(target, convertedComponent);
    }

    @SneakyThrows
    @Override
    public void showTitle(@NotNull Title title) {
        Object convertedTitle = getCachedOrConvert(title, () -> {
            Times times = title.times();
            if (times == null) times = Title.DEFAULT_TIMES;
            Object convertedTimes = TITLE_TIMES_FACTORY_METHOD.invoke(
                    times.fadeIn(),
                    times.stay(),
                    times.fadeOut());
            return TITLE_FACTORY_METHOD.invoke(
                    convertComponent(title.title()),
                    convertComponent(title.subtitle()),
                    convertedTimes);
        });
        SHOW_TITLE_METHOD.invoke(target, convertedTitle);
    }

    private static Object convertComponent(@NotNull Component message) throws Throwable {
        return DESERIALIZE_METHOD.invoke(GSON_COMPONENT_SERIALIZER, serializeForNative(message));
    }

    public static Class<?> nativeComponentClass() {
        return COMPONENT_CLASS;
    }

    @SneakyThrows
    public static Object toNativeComponent(@NotNull Component component) {
        return convertComponent(component);
    }

    @SneakyThrows
    public static @NotNull Component fromNativeComponent(@NotNull Object nativeComponent) {
        Object json = SERIALIZE_METHOD.invoke(GSON_COMPONENT_SERIALIZER, nativeComponent);
        return COMPATIBLE_GSON.deserialize((String) json);
    }

    /**
     * Serializes a component to JSON that any native Adventure version can read
     * back without losing click/hover events. Package-private for testing.
     */
    static String serializeForNative(@NotNull Component message) {
        return COMPATIBLE_GSON.serialize(message);
    }

    private static Object convertSound(@NotNull Sound sound) throws Throwable {
        Object name = KEY_FACTORY_METHOD.invoke(sound.name().namespace(), sound.name().value());
        @SuppressWarnings({"rawtypes", "unchecked"})
        Object source = Enum.valueOf((Class<? extends Enum>) SOUND_SOURCE_CLASS, sound.source().name());
        return SOUND_FACTORY_METHOD.invoke(name, source, sound.volume(), sound.pitch());
    }

    private static Object getCachedOrConvert(Object object, UncheckedCallable<Object> callable)
            throws ExecutionException {
        return CONVERTED_OBJECTS_CACHE.get(object, callable);
    }

    interface UncheckedCallable<T> extends Callable<T> {

        @Override
        default T call() {
            try {
                return run();
            } catch (Throwable e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                }
                throw new RuntimeException(e);
            }
        }

        T run() throws Throwable;

    }

}
