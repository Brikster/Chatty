package ru.brikster.chatty.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@UtilityClass
public class CollectionUtil {

    @SafeVarargs
    public <T> List<T> listOf(Collection<T>... collections) {
        List<T> list = new ArrayList<>();
        for (Collection<T> collection : collections) {
            list.addAll(collection);
        }
        return list;
    }

}
