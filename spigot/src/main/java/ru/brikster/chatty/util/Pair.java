package ru.brikster.chatty.util;

import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor(staticName = "create")
@FieldDefaults(makeFinal = true)
public class Pair<A, B> {

    A a;
    B b;

    public A a() { return a; }

    public B b() { return b; }

}
