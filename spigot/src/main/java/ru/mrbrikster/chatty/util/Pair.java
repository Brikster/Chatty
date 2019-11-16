package ru.mrbrikster.chatty.util;

import lombok.Getter;

public class Pair<K, V> {

    @Getter private final K key;
    @Getter private final V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

}
