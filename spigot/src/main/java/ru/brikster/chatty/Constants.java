package ru.brikster.chatty;

import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class Constants {

    public Pattern REPLACEMENTS_PATTERN = Pattern.compile("\\{r_[a-z0-9_]{1,24}}");

}
