# Chatty (Bukkit)
[![Build Status](https://ci.minemoon.ru/job/chatty/badge/icon)](https://ci.minemoon.ru/job/chatty/)

Chatty is a unique Bukkit-plugin, that supports all modern Bukkit-servers, such as Thermos, Cauldron, Spigot, PaperSpigot e t.c. This plugin doesn't have any non-switchable options. It's simple, stable and lightweight.

  - Chat-modes, such as local and global chats. Also you can add another chat-mode.
  - Chat-modes separation by permission. For example, if you have permission "chatty.chat.local", but have "chatty.chat.global", your message will sent at global chat.
  - SPY-mode permission. Players with "chatty.spy" permission can see all messages from all chat-modes.
  - Vault API support.
  - Advancements announcements system.
  - Cooldowns for chat-modes.
  - Auto-messages system.
  
# Permissions
    chatty.chat.<chat_mode> (chatty.chat.<chat_mode>.see or chatty.chat.<chat_mode>.send) - grants access for chat-mode.
    chatty.spy.<chat_mode> or chatty.spy - allows to see all messages from chat-modes.
    chatty.command.spy - allows to use "/spy" command for enabling/disabling spy-mode.
    chatty.command.reload - allows to use "/chatty" command for reloading configuration.
    chatty.alerts.<list> - allows to see messages from alert list.
    chatty.style.<style> or chatty.style.<style>.<chat-mode> - allows to use styles in chat
        (styles: colors, bold, magic, reset, italic, underline, strikethrough).
    chatty.cooldown or chatty.cooldown.<chat-mode> - allows to bypass cooldown of chat-mode.

# Configuration
See in <b><u>src/main/java/resources/config.yml.</u></b>

# Credits
Supports by McStudio.
