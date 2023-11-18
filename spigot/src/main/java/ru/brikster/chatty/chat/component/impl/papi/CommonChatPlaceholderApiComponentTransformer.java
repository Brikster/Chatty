package ru.brikster.chatty.chat.component.impl.papi;

import ru.brikster.chatty.convert.component.ComponentStringConverter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.function.Function;
import java.util.regex.Pattern;

@Singleton
public final class CommonChatPlaceholderApiComponentTransformer extends AbstractPlaceholderApiComponentTransformer {

    @Inject
    public CommonChatPlaceholderApiComponentTransformer(ComponentStringConverter componentStringConverter) {
        super(componentStringConverter, Pattern.compile("%([^%]+)%"), Function.identity());
    }

}
