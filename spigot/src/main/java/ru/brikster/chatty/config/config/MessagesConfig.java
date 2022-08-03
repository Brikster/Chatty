package ru.brikster.chatty.config.config;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.annotation.NameModifier;
import eu.okaeri.configs.annotation.NameStrategy;
import eu.okaeri.configs.annotation.Names;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import lombok.Getter;

@Getter
@SuppressWarnings("FieldMayBeFinal")
@Names(strategy = NameStrategy.HYPHEN_CASE, modifier = NameModifier.TO_LOWER_CASE)
public class MessagesConfig extends OkaeriConfig {

    @Exclude
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private Component chatNotFound = MINI_MESSAGE.deserialize("<red>No matching chat found. Maybe you don't have enough permissions?");
    private Component nobodyHeard = MINI_MESSAGE.deserialize("<red>Nobody heard you.");

}
