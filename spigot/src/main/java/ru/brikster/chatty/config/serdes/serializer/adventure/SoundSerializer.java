package ru.brikster.chatty.config.serdes.serializer.adventure;

import eu.okaeri.configs.schema.GenericsDeclaration;
import eu.okaeri.configs.serdes.DeserializationData;
import eu.okaeri.configs.serdes.ObjectSerializer;
import eu.okaeri.configs.serdes.SerializationData;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Source;

import lombok.NonNull;

public class SoundSerializer implements ObjectSerializer<Sound> {

    @Override
    public boolean supports(@NonNull final Class<? super Sound> type) {
        return Sound.class.isAssignableFrom(type);
    }

    @Override
    public void serialize(@NonNull final Sound sound, @NonNull final SerializationData data, @NonNull final GenericsDeclaration generics) {
        data.add("name", sound.name().asString());
        data.add("source", sound.source());
        data.add("volume", sound.volume());
        data.add("pitch", sound.pitch());
    }

    @SuppressWarnings("PatternValidation")
    @Override
    public Sound deserialize(@NonNull final DeserializationData data, @NonNull final GenericsDeclaration generics) {
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
