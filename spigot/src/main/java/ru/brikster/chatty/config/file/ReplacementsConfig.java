package ru.brikster.chatty.config.file;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.*;
import lombok.Getter;
import ru.brikster.chatty.BuildConstants;

import java.util.Map;

@Getter
@SuppressWarnings("FieldMayBeFinal")
@Header("################################################################")
@Header("#")
@Header("#    Chatty (version " + BuildConstants.VERSION + ")")
@Header("#    Author: Brikster")
@Header("#")
@Header("################################################################")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class ReplacementsConfig extends OkaeriConfig {

    @Comment(value = {
            "",
            "List of useful components for replacements."}, language = "en-US")
    @Comment(value = {
            "",
            "Список полезных компонентов для замен."}, language = "ru-RU")
    @Comment(value = "You can use it as placeholder in any config with {r_key}, e.g. {r_player_info}.", language = "en-US")
    @Comment(value = "Их можно вставлять в любой конфиг как {r_ключ}, например {r_player_info}.", language = "ru-RU")
    private Map<String, String> replacements = Map.of(
            "player_info",
            "<hover:'&2Click here to PM {player}'><click:'suggest_command':'/msg {player} '>{player}</click></hover>"
    );

}
