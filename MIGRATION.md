# Migrating from Chatty v2 to v3

Chatty v3 is a ground-up rewrite. Its configuration is split into several
files (`settings.yml`, `chats.yml`, `pm.yml`, `moderation.yml`, `vanilla.yml`,
`notifications.yml`, `replacements.yml`, `proxy.yml`) and a `lang/` folder,
instead of the single v2 `config.yml`.

> Russian version: [MIGRATION.ru.md](MIGRATION.ru.md)

## Automatic migration

When v3 starts and finds a legacy v2 `config.yml` in `plugins/Chatty/`, it:

1. Renames the whole folder to `Chatty_old_<timestamp>/` (your old config is
   never deleted).
2. Generates fresh v3 config files.
3. Migrates everything it can map unambiguously into those files.
4. Logs a summary of what still needs manual attention.

**Migrated automatically:**

| v2 | v3 |
|----|----|
| `chats.<id>` — format, display-name, symbol, range, cooldown, permission, command, aliases | `chats.yml` → `chats.<id>` |
| `general.locale` | `settings.yml` → `language` |
| `general.priority` | `settings.yml` → `listener-priority` |
| `general.keep-old-recipients` | `settings.yml` → `respect-foreign-recipients` |
| `general.hide-vanished-recipients` | `settings.yml` → `hide-vanished-recipients` |
| `json.mentions.enable` / `.format` | `settings.yml` → `mentions.enable` / `mentions.target-format` |
| `moderation.caps` / `advertisement` / `swear` | `moderation.yml` |
| `pm.enable`, `pm.allow-console`, `pm.format.*` | `pm.yml` |
| `miscellaneous.vanilla.{join,quit,death}` | `vanilla.yml` |

Notes:
- Chat `range: -3` (cross-server) is preserved. v2 sent those chats over
  BungeeCord and v3 uses Redis, so configure `proxy.yml` — see below.
- Chat `cooldown: -1` (disabled) becomes `0`.
- Chats disabled in v2 (`enable: false`) are not migrated.
- PM format placeholders are translated: `{sender-*}` → `{from-*}`,
  `{recipient-*}` → `{to-*}`.
- Mention format placeholder `{player}` is translated to `{username}`.
- `caps.length` and `caps.percent` are brought into the range v3 accepts:
  v2 allowed `length: 0` and a percent above 100, which v3 rejects.

## Needs manual attention

These are **not** migrated automatically and stay at v3 defaults:

- **Sounds** — v2 used Bukkit sound names (`CLICK`, `ORB_PICKUP`); v3 uses
  Adventure sound keys. Re-set the `sound` options where needed.
- **Interactive replacements** (`json.replacements`) — v3 uses a simpler
  MiniMessage format in `replacements.yml`.
- **Notifications** (`notifications.*`) — re-create them in `notifications.yml`.
- **Locale messages** — v2 `locale/*.yml` → v3 `lang/<language>.yml`. Your v2
  `general.locale` is migrated to `settings.yml` → `language` (`ru` → `ru-RU`,
  `zh_CN` → `zh-CN`, and so on); an unsupported value is reported in the
  startup notes and leaves the default in place.
- **Per-chat moderation toggles** — no direct v3 equivalent.
- **Cross-server chat** — v3 uses Redis; configure `proxy.yml`.

## Permissions

Most v2 nodes still work: v3 checks the legacy spelling alongside the new one,
so the groups below need no changes.

| v2 node | v3 node | status |
| --- | --- | --- |
| `chatty.chat.<chat>.see` | `chatty.chat.<chat>.read` | both accepted |
| `chatty.cooldown.<chat>` | `chatty.bypass.cooldown.<chat>` | both accepted |
| `chatty.moderation.caps` | `chatty.bypass.moderation.caps` | both accepted |
| `chatty.moderation.swear` | `chatty.bypass.moderation.swear` | both accepted |
| `chatty.moderation.advertisement` | `chatty.bypass.moderation.ads` | both accepted |
| `chatty.command.msg` | `chatty.pm` | both accepted |
| `chatty.command.reply` | `chatty.pm` | both accepted |
| `chatty.notification.actionbar` | `chatty.notification.actionbar.<name>` | both accepted |

### Must be re-granted

`chatty.style.*` no longer means what it did. In v2 it let a player *write*
colour and formatting codes; in v3 the same-looking node selects a named chat
format from `chats.yml`, and writing codes moved to `chatty.decoration.*`.
**Remove your `chatty.style.*` wildcards** — left in place they now match every
named style in `chats.yml`, including ones you meant to sell — and grant the
replacements explicitly:

| v2 node | v3 node |
| --- | --- |
| `chatty.style.colors` | `chatty.decoration.color` |
| `chatty.style.bold` | `chatty.decoration.bold` |
| `chatty.style.italic` | `chatty.decoration.italic` |
| `chatty.style.underline` | `chatty.decoration.underlined` |
| `chatty.style.strikethrough` | `chatty.decoration.strikethrough` |
| `chatty.style.magic` | `chatty.decoration.obfuscated` |
| `chatty.style.reset` | `chatty.decoration.reset` |
| `chatty.spy` | `chatty.spy.pm` |

v2's per-chat form `chatty.style.<style>.<chat>` has an equivalent, spelled
inside the chat's own namespace: `chatty.chat.<chat>.decoration.<style>`. So
`chatty.style.colors.global` becomes `chatty.chat.global.decoration.color`, and
`chatty.decoration.color` without a chat still grants every chat as before.
The chat cannot go last, the way v2 wrote it, because that position already
names a colour — `chatty.decoration.color.red` means "may use red".

`chatty.decoration` on its own also grants hex colours, which v2 never allowed
in player messages — grant the individual leaves unless you want that.

### Gone with the feature

`chatty.command.chat`, `chatty.command.prefix[.others]`,
`chatty.command.suffix[.others]`, `chatty.command.swears` and
`chatty.swears.see` have no v3 equivalent.

Toast notifications are back, under a new node: v2's
`chatty.notification.advancements.<name>` becomes
`chatty.notification.advancement.<name>`, and the channels are configured in
`notifications.yml` under `advancements`.

### Note on /clearchat

`/clearchat` clears only your own screen, as in v2. Clearing chat for everyone
is `/clearchat all` and needs `chatty.command.clearchat.all`.

## Requirements

- **Java 11 or newer.** The plugin will not load on Java 8.
- **Minecraft 1.8.8 or newer.**

Stay on 2.19.14 if your server does not meet these.

## Tips

- v3 still understands legacy `&` color codes, plus MiniMessage and hex
  formats — your v2 formats keep working after migration.
- Review the migrated files and the startup log before going live.
- Keep the `Chatty_old_<timestamp>/` backup until you are happy with v3.
- To send a message into one specific chat from the console or a scheduler, use
  `/chatty broadcast <chat> <message>`.
