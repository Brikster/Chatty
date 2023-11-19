package ru.brikster.chatty.chat.component.impl.pm.prefix;

import ru.brikster.chatty.chat.component.impl.prefix.AbstractPrefixComponentTransformer;
import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.prefix.PrefixProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

@Singleton
public final class PmFromPrefixComponentTransformer extends AbstractPrefixComponentTransformer {

    @Inject
    public PmFromPrefixComponentTransformer(PrefixProvider prefixProvider,
                                      ComponentStringConverter componentStringConverter) {
        super(prefixProvider, componentStringConverter,
                Pattern.compile("\\{from-prefix}|\\{from-suffix}"),
                "{from-prefix}", "{from-suffix}");
    }
}
