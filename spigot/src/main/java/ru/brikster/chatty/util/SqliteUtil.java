package ru.brikster.chatty.util;

import lombok.experimental.UtilityClass;

import java.nio.ByteBuffer;
import java.util.UUID;

@UtilityClass
public class SqliteUtil {

    public UUID toUUID(byte[] bytes) {
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        return new UUID(buf.getLong(), buf.getLong());
    }

    public byte[] fromUUID(UUID uuid) {
        byte[] bytes = new byte[16];
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        buf.putLong(uuid.getMostSignificantBits());
        buf.putLong(uuid.getLeastSignificantBits());
        return bytes;
    }

}
