package ru.brikster.chatty.config.file;

import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeathCausesTest {

    @Test
    void everyShippedCauseNamesARealDamageCause() {
        Set<String> known = Arrays.stream(DamageCause.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        Set<String> unknown = new MessagesConfig().getDeathCauses().keySet().stream()
                .filter(key -> !known.contains(key))
                .collect(Collectors.toCollection(TreeSet::new));

        assertTrue(unknown.isEmpty(),
                "a key that is not a DamageCause never matches, so the death message"
                        + " silently falls back: " + unknown);
    }

    @Test
    void theCommonWaysToDieAreCovered() {
        Set<String> causes = new MessagesConfig().getDeathCauses().keySet();

        for (DamageCause cause : Set.of(DamageCause.ENTITY_ATTACK, DamageCause.FALL,
                DamageCause.DROWNING, DamageCause.LAVA, DamageCause.FIRE, DamageCause.VOID,
                DamageCause.PROJECTILE, DamageCause.SUFFOCATION, DamageCause.STARVATION)) {
            assertTrue(causes.contains(cause.name()), cause + " has no text");
        }
    }

    @Test
    void thereIsAlwaysSomethingToFallBackOn() {
        assertFalse(new MessagesConfig().getDeathFallbackCause().isBlank(),
                "an unmatched cause would leave the death message with an empty {cause}");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ru-RU", "de-DE", "es-ES", "zh-CN"})
    void everyBundledLanguageTranslatesTheSameCauses(String language) throws Exception {
        Set<String> expected = new TreeSet<>(new MessagesConfig().getDeathCauses().keySet());

        Map<String, Object> data;
        try (InputStream in = getClass().getResourceAsStream("/lang/" + language + ".yml")) {
            assertNotNull(in, "bundled lang/" + language + ".yml is missing");
            data = new Yaml().load(in);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> causes = (Map<String, Object>) data.get("death-causes");
        assertNotNull(causes, language + " has no death-causes section");

        Set<String> actual = new TreeSet<>(causes.keySet());
        assertTrue(actual.equals(expected),
                language + " translates a different set of causes than the defaults."
                        + " Missing: " + missing(expected, actual)
                        + ", unknown: " + missing(actual, expected));

        for (Map.Entry<String, Object> entry : causes.entrySet()) {
            assertFalse(String.valueOf(entry.getValue()).isBlank(),
                    language + " leaves " + entry.getKey() + " empty");
        }
        assertFalse(String.valueOf(data.get("death-fallback-cause")).isBlank(),
                language + " has no fallback cause");
    }

    private static Set<String> missing(Set<String> from, Set<String> in) {
        return from.stream().filter(key -> !in.contains(key))
                .collect(Collectors.toCollection(TreeSet::new));
    }

}
