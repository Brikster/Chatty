package ru.mrbrikster.chatty.util;

import lombok.Getter;

public class Pair<A, B> {

    @Getter private final A a;
    @Getter private final B b;

    private Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public static <A, B> Pair<A, B> of(A a, B b) {
        return new Pair<>(a, b);
    }

}
