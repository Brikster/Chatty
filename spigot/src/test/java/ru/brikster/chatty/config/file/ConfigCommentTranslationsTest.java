package ru.brikster.chatty.config.file;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigCommentTranslationsTest {

    private static final List<Class<? extends OkaeriConfig>> CONFIGS = List.of(
            ChatsConfig.class, MessagesConfig.class, ModerationConfig.class,
            NotificationsConfig.class, PmConfig.class, ProxyConfig.class,
            ReplacementsConfig.class, SettingsConfig.class, VanillaConfig.class);

    private static final Set<String> TRANSLATED = Set.of("en-US", "ru-RU");

    @Test
    void everyTranslatedCommentHasBothLanguages() {
        Set<String> lopsided = new TreeSet<>();

        for (Class<?> config : CONFIGS) {
            for (Class<?> declaring : withNestedClasses(config)) {
                for (Field field : declaring.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    Set<String> languages = Arrays.stream(field.getAnnotationsByType(Comment.class))
                            .map(Comment::language)
                            .filter(TRANSLATED::contains)
                            .collect(Collectors.toCollection(TreeSet::new));

                    if (!languages.isEmpty() && !languages.equals(TRANSLATED)) {
                        lopsided.add(declaring.getSimpleName() + "." + field.getName() + " has " + languages);
                    }
                }
            }
        }

        assertTrue(lopsided.isEmpty(),
                "these fields would show a comment in one language and none in the other: " + lopsided);
    }

    @Test
    void noCommentIsLeftUntranslated() {
        Set<String> untranslated = new TreeSet<>();

        for (Class<?> config : CONFIGS) {
            for (Class<?> declaring : withNestedClasses(config)) {
                for (Field field : declaring.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    for (Comment comment : field.getAnnotationsByType(Comment.class)) {
                        boolean hasText = Arrays.stream(comment.value()).anyMatch(line -> !line.isBlank());
                        if (hasText && !TRANSLATED.contains(comment.language())) {
                            untranslated.add(declaring.getSimpleName() + "." + field.getName());
                        }
                    }
                }
            }
        }

        assertTrue(untranslated.isEmpty(),
                "these comments render in every language, so a Russian server sees English: " + untranslated);
    }

    private static List<Class<?>> withNestedClasses(Class<?> root) {
        List<Class<?>> classes = new ArrayList<>();
        classes.add(root);
        for (Class<?> nested : root.getDeclaredClasses()) {
            classes.addAll(withNestedClasses(nested));
        }
        return classes;
    }

}
