# Chatty (Bukkit)

Chatty is a unique Bukkit-plugin, that supports all modern Bukkit-servers, such as Thermos, Cauldron, Spigot, PaperSpigot e t.c. This plugin doesn't have any non-switchable options. It's simple, stable and lightweight.

  - Chat-modes, such as local and global chats. Also you can add another chat-mode.
  - Chat-modes separation by permission. For example, if you have permission "chatty.chat.local", but have "chatty.chat.global", your message will sent at global chat.
  - SPY-mode permission. Players with "chatty.spy" permission can see all messages from all chat-modes.
  - Vault API support.

# Permissions
    chatty.chat.<chat_mode> - grants access for chat-mode.
    chatty.spy - allows to see all messages from all chat-modes.
    chatty.reload - allows to use "/chatty" command for reloading configuration.


# Configuration
    # GENERAL.
    # Priority: priority of event handler:
    # lowest, low, normal, high or highest.
    #
    # Log: save logs of chat?
    #
    # Spy: sends all messages to players with "chatty.spy" permission.
    general:
      priority: normal
      log: true
      spy: true

    # CHAT MODES
    # You need to enable at least one.
    #
    # Range: range of chat in blocks.
    # -1 to disable.
    #
    # Symbol: symbol to use this chat.
    # Empty symbol to set the chat as default.
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

      # Global chat.
      # Permission: chatty.chat.global
      global:
        enable: true
        format: '[Global] {prefix}{player}{suffix}: {message}'
        range: -1
        symbol: '!'

    # MESSAGES
    messages:
      no-chat-mode: '&cApplicable chat-mode not found. You can''t send the message'
      reload: '&aConfig successful reloaded!'
      no-permission: '&cYou don''t have permission.'

### Credits
Supported by McStudio.
