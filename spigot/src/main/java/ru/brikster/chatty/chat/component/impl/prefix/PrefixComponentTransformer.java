package ru.brikster.chatty.chat.component.impl.prefix;

import ru.brikster.chatty.convert.component.ComponentStringConverter;
import ru.brikster.chatty.prefix.PrefixProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

@Singleton
public final class PrefixComponentTransformer extends AbstractPrefixComponentTransformer {

    @Inject
    public PrefixComponentTransformer(PrefixProvider prefixProvider,
                                      ComponentStringConverter componentStringConverter) {
        super(prefixProvider, componentStringConverter,
                Pattern.compile("\\{prefix}|\\{suffix}"),
                "{prefix}", "{suffix}");
    }

}
