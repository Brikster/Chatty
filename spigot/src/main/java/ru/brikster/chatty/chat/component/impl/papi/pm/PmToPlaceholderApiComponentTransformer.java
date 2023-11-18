package ru.brikster.chatty.chat.component.impl.papi.pm;

import ru.brikster.chatty.chat.component.impl.papi.AbstractPlaceholderApiComponentTransformer;
import ru.brikster.chatty.convert.component.ComponentStringConverter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

@Singleton
public final class PmToPlaceholderApiComponentTransformer extends AbstractPlaceholderApiComponentTransformer {

    private static final String FROM_PREFIX = Pattern.quote("%to:");

    @Inject
    public PmToPlaceholderApiComponentTransformer(ComponentStringConverter componentStringConverter) {
        super(componentStringConverter, Pattern.compile("%to:([^%]+)%"), matchedString ->
                matchedString.replaceFirst(FROM_PREFIX, "%"));
    }

}
