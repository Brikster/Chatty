package ru.brikster.chatty.chat.component.impl.papi.pm;

import ru.brikster.chatty.chat.component.impl.papi.AbstractPlaceholderApiComponentTransformer;
import ru.brikster.chatty.convert.component.ComponentStringConverter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

@Singleton
public final class PmFromPlaceholderApiComponentTransformer extends AbstractPlaceholderApiComponentTransformer {

    private static final String FROM_PREFIX = Pattern.quote("%from:");

    @Inject
    public PmFromPlaceholderApiComponentTransformer(ComponentStringConverter componentStringConverter) {
        super(componentStringConverter, Pattern.compile("%from:([^%]+)%"), matchedString ->
                matchedString.replaceFirst(FROM_PREFIX, "%"));
    }

}
