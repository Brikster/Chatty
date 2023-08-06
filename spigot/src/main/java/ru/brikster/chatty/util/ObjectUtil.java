package ru.brikster.chatty.util;

import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class ObjectUtil {

    public static <T> T requireNonNullElse(T obj, T defaultObj) {
        return (obj != null) ? obj : Objects.requireNonNull(defaultObj, "defaultObj");
    }
}
