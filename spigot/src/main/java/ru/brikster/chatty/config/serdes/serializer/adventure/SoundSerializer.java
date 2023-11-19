package ru.brikster.chatty.config.serdes.serializer.adventure;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;
import org.jetbrains.annotations.NotNull;

public final class SoundSerializer implements ObjectSerializer<Sound> {

    @Override
    public boolean supports(@NotNull final Class<? super Sound> type) {
        return Sound.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NotNull final Sound sound, @NotNull final SerializationData data, @NotNull final GenericsDeclaration generics) {
        data.add("name", sound.name().asString());
        data.add("source", sound.source());
        data.add("volume", sound.volume());
        data.add("pitch", sound.pitch());
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Sound deserialize(@NotNull final DeserializationData data, @NotNull final GenericsDeclaration generics) {
        String key = data.get("name", String.class);

        Source source;
        if (data.containsKey("source")) {
            source = data.get("source", Source.class);
        } else {
            source = Source.MASTER;
        }

        float volume = data.get("volume", float.class);
        float pitch = data.get("pitch", float.class);

        return Sound.sound(Key.key(key), source, volume, pitch);
    }

}
