package ru.brikster.chatty.chat.component.impl.pm.placeholders;

import ru.brikster.chatty.chat.component.impl.papi.AbstractPlaceholderApiComponentTransformer;
import ru.brikster.chatty.config.file.SettingsConfig;
import ru.brikster.chatty.convert.component.ComponentStringConverter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

@Singleton
public final class PmToPlaceholderApiComponentTransformer extends AbstractPlaceholderApiComponentTransformer implements PmToPlaceholdersComponentTransformer {

    private static final String FROM_PREFIX = Pattern.quote("%to:");

    @Inject
    public PmToPlaceholderApiComponentTransformer(ComponentStringConverter componentStringConverter,
                                                  SettingsConfig settingsConfig) {
        super(componentStringConverter, settingsConfig, Pattern.compile("%to:([^%]+)%"), matchedString ->
                matchedString.replaceFirst(FROM_PREFIX, "%"));
    }

}
