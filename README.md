# Chatty (Bukkit)

Chatty is a unique Bukkit-plugin, that supports all modern Bukkit-servers, such as Thermos, Cauldron, Spigot, PaperSpigot e t.c. This plugin doesn't have any non-switchable options. It's simple, stable and lightweight.

  - Chat-modes, such as local and global chats. Also you can add another chat-mode.
  - Chat-modes separation by permission. For example, if you have permission "chatty.chat.local", but have "chatty.chat.global", your message will sent at global chat.
  - SPY-mode permission. Players with "chatty.spy" permission can see all messages from all chat-modes.
  - Vault API support.
  - Advancements announcements system.
  - Cooldowns for chat-modes.

# Permissions
    chatty.chat.<chat_mode> - grants access for chat-mode.
    chatty.spy - allows to see all messages from all chat-modes.
    chatty.reload - allows to use "/chatty" command for reloading configuration.
    chatty.style.<style> or chatty.style.<style>.<chat-mode> - allows to use styles in chat
        (styles: colors, bold, magic, reset, italic, underline, strikethrough).
    chatty.cooldown or chatty.cooldown.<chat-mode> - allows to bypass cooldown of chat-mode.


# Configuration
    # GENERAL
    # Priority: priority of event handler:
    # lowest, low, normal, high or highest.
    #
    # Log: save logs of chat?
    #
    # Spy: sends all messages to players with "chatty.spy" permission.
    # Format: format of spy messages.
    general:
      priority: low
      log: true
      spy:
        enable: true
        format: '&6[Spy] &r{format}'
    
    # CHAT MODES
    # You need to enable at least one.
    #
    # Range: range of chat in blocks.
    # -1 to disable.
    #
    # Symbol: symbol to use this chat.
    # Empty symbol to set the chat as default.
    #
    # Cooldown: cooldown for this chat in seconds.
    #
    # ******************************************
    # You can create any types of chats, not only "local" or "global".
    chats:
      # Local chat.
      # Permission: chatty.chat.local
      local:
        enable: true
        format: '[Local] {prefix}{player}{suffix}: {message}'
        range: 100
        symbol: ''
        cooldown: -1
    
      # Global chat.
      # Permission: chatty.chat.global
      global:
        enable: true
        format: '[Global] {prefix}{player}{suffix}: {message}'
        range: -1
        symbol: '!'
        cooldown: -1
    
    # ANNOUNCEMENTS
    # Messages in new "Advancements" notifications
    #
    # WARNING: 1.12 and higher.
    announcements:
      enable: false
      # Repeating time in seconds.
      time: 60
      list:
      - icon: 'minecraft:cobblestone'
        header: '&6Header'
        footer: '&7Message text #1'
      - icon: 'minecraft:apple'
        header: '&6Header'
        footer: '&7Message text #2'
    
    # MESSAGES
    messages:
      no-chat-mode: '&cApplicable chat-mode not found. You can''t send the message'
      reload: '&aConfig successful reloaded!'
      no-permission: '&cYou don''t have permission.'
      spy-on: '&aYou have been enabled spy-mode.'
      spy-off: '&cYou have been disabled spy-mode.'
      cooldown: '&cWait for {cooldown} seconds, before send message in this chat again.'

# Credits
Supports by McStudio.
