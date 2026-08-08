package ru.brikster.chatty.config.file;

import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShippedSenderFormatPermissionsTest {

    @Test
    void everyShippedSenderFormatIsDeniedByDefault() {
        Map<String, Object> permissions = declaredPermissions();

        shippedSenderFormats().forEach((chatId, ids) -> ids.forEach(id -> {
            assertDeclaredAsFalse(permissions, "chatty.sender-format." + id);
            assertDeclaredAsFalse(permissions, "chatty.chat." + chatId + ".sender-format." + id);
        }));
    }

    @Test
    void theShippedDefaultsActuallyCarryExamples() {
        assertFalse(shippedSenderFormats().isEmpty(),
                "no example would mean this test guards nothing");
    }

    private static void assertDeclaredAsFalse(Map<String, Object> permissions, String node) {
        Object declaration = permissions.get(node);
        assertNotNull(declaration, node + " is undeclared, so every operator holds it");

        @SuppressWarnings("unchecked")
        Object value = ((Map<String, Object>) declaration).get("default");
        assertEquals(Boolean.FALSE, value,
                node + " must default to false, or operators match the shipped example");
    }

    private static Map<String, Set<String>> shippedSenderFormats() {
        return new ChatsConfig().getChats().entrySet().stream()
                .filter(entry -> !entry.getValue().getSenderFormats().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey,
                        entry -> entry.getValue().getSenderFormats().keySet()));
    }

    private static Map<String, Object> declaredPermissions() {
        try (InputStream stream = ShippedSenderFormatPermissionsTest.class
                .getClassLoader().getResourceAsStream("plugin.yml")) {
            assertNotNull(stream, "plugin.yml is missing from the resources");

            Map<String, Object> pluginYml = new Yaml().load(stream);
            @SuppressWarnings("unchecked")
            Map<String, Object> permissions = (Map<String, Object>) pluginYml.get("permissions");
            assertNotNull(permissions, "plugin.yml declares no permissions block");
            assertTrue(permissions.size() > 1);
            return permissions;
        } catch (Exception e) {
            throw new IllegalStateException("Cannot read plugin.yml", e);
        }
    }

}
