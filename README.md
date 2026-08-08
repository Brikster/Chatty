<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset=".github/assets/logo-dark.svg">
    <img src=".github/assets/logo.svg" alt="Chatty" width="420">
  </picture>
</p>

# Chatty (Bukkit plugin)

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/Brikster/Chatty)](https://github.com/Brikster/Chatty/releases/latest)
[![GitHub All Releases](https://img.shields.io/github/downloads/Brikster/Chatty/total)](https://github.com/Brikster/Chatty/releases)
[![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/Brikster/Chatty)](https://github.com/Brikster/Chatty/archive/master.zip)
[![JitPack](https://jitpack.io/v/Brikster/Chatty.svg)](https://jitpack.io/#Brikster/Chatty)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/815bf25f21da4c81b9e26bd1159df072)](https://www.codacy.com/gh/Brikster/Chatty/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Brikster/Chatty&amp;utm_campaign=Badge_Grade)

> **Chatty v3** is a ground-up rewrite built on Kyori's Adventure library. It is
> approaching its first stable release (`3.0.0`); this branch holds its code.
>
> - **Stable builds** — the [Releases](https://github.com/Brikster/Chatty/releases) page (once `3.0.0` is tagged).
> - **Development builds** — the latest artifact from the "Actions" tab (see the "Artifacts" section).
>
> Chatty v2.* is deprecated and no longer maintained.
> Upgrading from v2? See [MIGRATION.md](MIGRATION.md) — v3 migrates your old config automatically.

Chatty is the modern chat management system for Bukkit-compatible servers. It's based on-top of Kyori's Adventure library, 
that makes it so powerful and stable.

**Key features**:
- Chat channels ("local" and "global" by default)
- Private messaging
- Moderation (CAPS, advertisements, swears)
- Notifications (chat, action bar and title)
- "Vanilla" messages configuring (join/quit/death)
- MiniMessage both legacy (&) styling format

## Moderation

Caps, advertisement and swear filters, plus mute:

```
/mute <player> [10m|2h|3d|1w] [reason]    # no duration means permanent
/unmute <player>
```

Mutes are stored with the player, so they survive a restart and apply on every
server sharing the database. A muted player cannot use public chats or private
messages. `chatty.command.mute` grants the commands, `chatty.bypass.mute` exempts
a player — operators hold both by default.

## Placeholders

With PlaceholderAPI installed, Chatty exposes its own data so TAB, scoreboards,
Discord bridges and anything else can read it:

| Placeholder | Value |
| --- | --- |
| `%chatty_chat%` | id of the chat the player currently writes to |
| `%chatty_chat_displayname%` | its display name |
| `%chatty_chat_range%` | its range in blocks (`-3` cross-server, `-2` server, `-1` world) |
| `%chatty_chats%` | every chat the player may write to |
| `%chatty_prefix%` / `%chatty_suffix%` | the prefix and suffix Chatty resolves for the player |
| `%chatty_spy%` | whether spy mode is on |
| `%rel_chatty_ignore%` | whether the first player ignores the second |

`%chatty_prefix%` is empty unless Vault or LuckPerms is installed, because that
is where the prefix comes from.

## Using the API

Add the API as a **compileOnly** dependency and declare Chatty as a plugin
dependency — the classes come from the running plugin at runtime:

```groovy
repositories { maven { url = 'https://jitpack.io' } }
dependencies { compileOnly 'ru.brikster:chatty-api:3.0.0' }
```

```yaml
depend: [ Chatty ]
```

The published artifact carries Chatty's relocated Adventure, because the plugin
bundles its own copy to keep working on servers that have none. That is why it
must be `compileOnly`: a second copy on your own classpath would be a different
class to the JVM. Sources and javadoc jars are published alongside it.

## Platforms

Paper, Spigot and Purpur from 1.8.8 up to 26.x, and Folia. Folia support is
verified by booting a real Folia server: the plugin schedules its own work and
never touches the Bukkit scheduler, and the bundled bStats, which does, is
skipped there.

## Text formatting

Every spelling below is covered by `ColourSpellingMatrixTest`, so this table is
what the code does rather than what it intends to do. All of them work in chat
formats, in `message-format`, in `lang/` files and in a prefix or suffix coming
from LuckPerms or Vault.

| Spelling | Example | Supported |
| --- | --- | --- |
| Legacy colour | `&c` | yes |
| Legacy decoration | `&l` `&n` `&o` `&m` `&k` `&r` | yes |
| Section sign | `§c` | yes |
| MiniMessage colour | `<red>` | yes |
| MiniMessage hex | `<#757575>` | yes |
| Ampersand hex | `&#757575` | yes |
| Spigot spread hex | `&x&7&5&7&5&7&5` | yes |
| Gradient | `<gradient:#ff0000:#00ff00>` | yes |
| Rainbow | `<rainbow>` | yes |
| Bare hash | `#757575` | no, prints as text |

Writing colour codes in your own messages is a separate question — that needs
`chatty.decoration.*`, see the permissions section of the migration guide.

## Building

Chatty uses Gradle to handle dependencies & building. Building needs JDK 21;
the jar it produces targets Java 11, so it runs on Java 11 and newer.

### Compiling from source

```shell script
git clone https://github.com/Brikster/Chatty.git
cd Chatty/
./gradlew build
```

Output jar will be placed into `/build/libs` directory.

## Testing

Run the unit tests:

```shell script
./gradlew test
```

Run the end-to-end smoke test — it boots real Minecraft servers with the built
plugin and verifies that it enables cleanly on a fresh install, processes live
in-game chat, correctly migrates a legacy v2 configuration, still runs on a
legacy server (1.8.8), and coexists with DiscordSRV:

```shell script
./gradlew build
JAVA_HOME=/path/to/jdk-21 bash scripts/smoke-test.sh
```

`JAVA_HOME` must point at a JDK the target server accepts: 21 for 1.21.x, 11 for
1.16.5, 25 for 26.x. Pick the server version with `MC_VERSION`, and switch off
the parts a lane cannot run:

```shell script
MC_VERSION=26.2 CHAT_TEST=0 JAVA_HOME=/path/to/jdk-25 bash scripts/smoke-test.sh
```

`CHAT_TEST=0` skips the in-game bot, which cannot join a server newer than
protocol 1.21.9, and `LEGACY_SCENARIO=0` skips the 1.8.8 lane. The legacy
scenario needs a Java 11 runtime; it is downloaded automatically, or point
`LEGACY_JAVA_HOME` at an existing one.

Both run automatically on every push via GitHub Actions, with the smoke test
as a matrix over 1.21.11, 1.16.5 and 26.2.
