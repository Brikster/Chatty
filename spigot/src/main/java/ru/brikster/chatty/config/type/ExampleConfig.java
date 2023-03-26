package ru.brikster.chatty.config.type;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

@Getter
@SuppressWarnings("FieldMayBeFinal")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class ExampleConfig extends OkaeriConfig {

    @Exclude
    private static final ExampleSubConfig anotherExampleSubConfigValue = new ExampleSubConfig("Custom awesome value", 2022);

    @Comment
    private List<ExampleSubConfig> subConfigsList = Arrays.asList(new ExampleSubConfig(), anotherExampleSubConfigValue);

    @Comment
    private Map<String, ExampleSubConfig> subConfigsMap = new HashMap<String, ExampleSubConfig>() {{
        put("first-map-key", new ExampleSubConfig());
        put("second-map-key", anotherExampleSubConfigValue);
    }};

    @Comment
    private List<Map<String, ExampleSubConfig>> subConfigsMapsList = Collections.singletonList(new HashMap<String, ExampleSubConfig>() {{
        put("first-map-key", new ExampleSubConfig());
        put("second-map-key", anotherExampleSubConfigValue);
    }});

    @Comment
    private Map<String, List<ExampleSubConfig>> subConfigsListsMaps = new HashMap<String, List<ExampleSubConfig>>() {{
        put("first-map-key", Arrays.asList(new ExampleSubConfig(), anotherExampleSubConfigValue));
        put("second-map-key", Arrays.asList(new ExampleSubConfig(), anotherExampleSubConfigValue));
    }};

    @Getter
    @SuppressWarnings("FieldMayBeFinal")
    @AllArgsConstructor
    @NoArgsConstructor
    @Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
    public static class ExampleSubConfig extends OkaeriConfig {

        @Comment("You can see this comment inside maps and collections")
        private String someKeyFirst = "Awesome value";

        @Comment("And this comment too")
        private int someKeySecond = 1337;

    }

}
