![alt text](https://i.imgur.com/8D5JcGn.png "Chatty")

# Chatty (Bukkit plugin)

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/Brikster/Chatty)](https://github.com/Brikster/Chatty/releases/latest)
[![GitHub All Releases](https://img.shields.io/github/downloads/Brikster/Chatty/total)](https://github.com/Brikster/Chatty/releases)
[![Build Status](https://travis-ci.org/Brikster/Chatty.svg?branch=master)](https://travis-ci.org/Brikster/Chatty)
[![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/Brikster/Chatty)](https://github.com/Brikster/Chatty/archive/master.zip)
[![JitPack](https://jitpack.io/v/Brikster/Chatty.svg)](https://jitpack.io/#Brikster/Chatty)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/815bf25f21da4c81b9e26bd1159df072)](https://www.codacy.com/gh/Brikster/Chatty/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Brikster/Chatty&amp;utm_campaign=Badge_Grade)

Chatty is the unique Bukkit-plugin, that supports all modern Bukkit-servers, such as Thermos, Cauldron, Spigot,
PaperSpigot e t.c. This plugin doesn't have any non-switchable options. It's simple, stable and lightweight.

- Chats, such as local and global chats. Also you can add another chat.
- Chats separation by permission. For example, if you have permission "chatty.chat.local", but have "chatty.chat.global"
  , your message will sent at global chat.
- Spy-mode permission. Players with "chatty.spy" permission can see all messages from all chats.
- Vault API support.
- Advancements announcements system.
- Cooldowns for chats.
- Auto-messages system.
- Private messages system.

## Permissions

    chatty.chat.<chat> (chatty.chat.<chat>.see or chatty.chat.<chat_mode>.send) - grants access for chat.
    chatty.spy.<chat> or chatty.spy - allows to see all messages from chat-modes.
    chatty.command.spy - allows to use "/spy" command for enabling/disabling spy-mode.
    chatty.command.reload - allows to use "/chatty" command for reloading configuration.
    chatty.command.msg - allows to use "/msg" command.
    chatty.command.reply - allows to use "/reply" command.
    chatty.style.<style> or chatty.style.<style>.<chat> - allows to use styles in chat
        (styles: colors, bold, magic, reset, italic, underline, strikethrough).
    chatty.notification.chat.<list> - allows to see messages from Chat notification list.
    chatty.notification.advancements.<list> - allows to see messages from Advancements notification list.
    chatty.notification.actionbar - allows to see messages from ActionBar notification.
    chatty.moderation.advertisement - bypass advertisement moderation.
    chatty.moderation.caps - bypass caps moderation.
    chatty.cooldown or chatty.cooldown.<chat-mode> - allows to bypass cooldown of chat.

## Comparison with ChatEx

| Feature                                  | ChatEx | Chatty |
| ---------------------------------------- | ------ | ------ |
| Vault support                            | +      | +      |
| Advertisement protection                 | +      | +      |
| PlaceholderAPI support                   | +      | +      |
| Old versions support (1.5.2+)            | -      | +      |
| BungeeCord support                       | -      | +      |
| Replacement of AutoMessage               | -      | +      |
| Advancements and ActionBar notifications | -      | +      |
| Custom chat groups                       | -      | +      |
| Cooldowns                                | -      | +      |
| Built-in spy-mode                        | -      | +      |
| Private messages                         | -      | +      |
| New 1.16+ hex color codes                | -      | +      |

## Hex color codes and gradient

<i>Warning: Early versions of Spigot/PaperSpigot may not be supported. Use only latest builds to correct work of Chatty
1.16 features</i>

Chatty supports new 1.16+ color codes. To add a new color code in your chat format, you need use the following pattern:

    {#12ABCD}text

{#12ABCD} is hex code. You also can combine it with style codes:

    {#ffffff}&ltext

### Gradient

You can create various multi-color gradient strings with this pattern:

    {#ffffff:#0039a6:#d52b1e This is awesome tricolor gradient}

Number of hex codes is unlimited:

    {#d818c4:#ae6be6:#0f7584:#7983a7:#a793ba:#34344b This is multicolor gradient}

![In-game gradient example](https://i.imgur.com/Z1iXJm8.png)

## Configuration

See default configuration in <b><u>src/main/java/resources/config.yml.</u></b>

### A little life-hack

You can delete all excess blocks and keep only needed features.

Plugin will work even with this simple config:

    chats:
      default:
        enable: true
        format: '{prefix}{player}{suffix}&r: {message}'
        permission: false

## API

Project provides particular Maven module with API classes.

You can get access to plugin API with <b>ChattyApi.get()</b> method.

Now plugin has only one event in API: <b>ChattyMessageEvent</b>, that calls when any player messages the chat.

## Building

Chatty uses Gradle to handle dependencies & building.

### Requirements

- Java 8 JDK or newer
- Git

### Compiling from source

```shell script
git clone https://github.com/Brikster/Chatty.git
cd Chatty/
./gradlew build
```

You can find the output jar in `/target` directory.

## Credits

Supports by MCSTUDIO
