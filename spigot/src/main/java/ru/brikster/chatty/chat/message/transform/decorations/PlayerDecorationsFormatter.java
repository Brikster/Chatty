package ru.brikster.chatty.chat.message.transform.decorations;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.CharacterAndFormat;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.brikster.chatty.util.CollectionUtil;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Singleton
public final class PlayerDecorationsFormatter {

    private static final Map<String, CharacterAndFormat> COLORS = new HashMap<>() {{
        put("black", CharacterAndFormat.BLACK);
        put("dark_blue", CharacterAndFormat.DARK_BLUE);
        put("dark_green", CharacterAndFormat.DARK_GREEN);
        put("dark_aqua", CharacterAndFormat.DARK_AQUA);
        put("dark_red", CharacterAndFormat.DARK_RED);
        put("dark_purple", CharacterAndFormat.DARK_PURPLE);
        put("gold", CharacterAndFormat.GOLD);
        put("gray", CharacterAndFormat.GRAY);
        put("dark_gray", CharacterAndFormat.DARK_GRAY);
        put("blue", CharacterAndFormat.BLUE);
        put("green", CharacterAndFormat.GREEN);
        put("aqua", CharacterAndFormat.AQUA);
        put("red", CharacterAndFormat.RED);
        put("light_purple", CharacterAndFormat.LIGHT_PURPLE);
        put("yellow", CharacterAndFormat.YELLOW);
        put("white", CharacterAndFormat.WHITE);
    }};

    private static final Map<String, CharacterAndFormat> DECORATIONS = new HashMap<>() {{
        put("obfuscated", CharacterAndFormat.OBFUSCATED);
        put("bold", CharacterAndFormat.BOLD);
        put("strikethrough", CharacterAndFormat.STRIKETHROUGH);
        put("underlined", CharacterAndFormat.UNDERLINED);
        put("italic", CharacterAndFormat.ITALIC);
        put("reset", CharacterAndFormat.RESET);
    }};

    private static final LegacyComponentSerializer FULL_SERIALIZER = LegacyComponentSerializer
            .builder()
            .character('&')
            .formats(CollectionUtil.listOf(COLORS.values(), DECORATIONS.values()))
            .hexColors()
            .build();

    public @NotNull Component formatMessageWithDecorations(@NotNull CommandSender sender, @NotNull String message) {
        if (sender.hasPermission("chatty.decoration")) {
            return FULL_SERIALIZER.deserialize(message);
        }

        var componentSerializerBuilder = LegacyComponentSerializer
                .builder()
                .character('&');

        List<CharacterAndFormat> formatList = new LinkedList<>();

        if (sender.hasPermission("chatty.decoration.color")) {
            formatList.addAll(COLORS.values());
        } else {
            for (var format : COLORS.entrySet()) {
                if (sender.hasPermission("chatty.decoration.color." + format.getKey())) {
                    formatList.add(format.getValue());
                }
            }
        }

        for (var format : DECORATIONS.entrySet()) {
            if (sender.hasPermission("chatty.decoration." + format.getKey())) {
                formatList.add(format.getValue());
            }
        }

        if (!sender.hasPermission("chatty.decoration.hex")) {
            componentSerializerBuilder.hexCharacter((char) 0);
        } else {
            componentSerializerBuilder.hexColors();
        }

        componentSerializerBuilder.formats(formatList);

        return componentSerializerBuilder
                .build()
                .deserialize(message);
    }

}
