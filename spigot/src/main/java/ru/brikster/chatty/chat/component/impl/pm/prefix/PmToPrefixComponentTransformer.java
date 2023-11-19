package ru.brikster.chatty.chat.component.impl.pm.prefix;

import ru.brikster.chatty.chat.component.impl.prefix.AbstractPrefixComponentTransformer;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.prefix.PrefixProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

@Singleton
public final class PmToPrefixComponentTransformer extends AbstractPrefixComponentTransformer {

    @Inject
    public PmToPrefixComponentTransformer(PrefixProvider prefixProvider,
                                          ComponentStringConverter componentStringConverter) {
        super(prefixProvider, componentStringConverter,
                Pattern.compile("\\{to-prefix}|\\{to-suffix}"),
                "{to-prefix}", "{to-suffix}");
    }
}
