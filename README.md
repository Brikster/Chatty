# Chatty (Bukkit plugin)

[![GitHub release (latest by date)](https://img.shields.io/github/v/release/Brikster/Chatty)](https://github.com/Brikster/Chatty/releases/latest)
[![GitHub All Releases](https://img.shields.io/github/downloads/Brikster/Chatty/total)](https://github.com/Brikster/Chatty/releases)
[![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/Brikster/Chatty)](https://github.com/Brikster/Chatty/archive/master.zip)
[![JitPack](https://jitpack.io/v/Brikster/Chatty.svg)](https://jitpack.io/#Brikster/Chatty)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/815bf25f21da4c81b9e26bd1159df072)](https://www.codacy.com/gh/Brikster/Chatty/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Brikster/Chatty&amp;utm_campaign=Badge_Grade)

> Latest release of Chatty (v2.*) is deprecated and won't be updated. 
> Currently we're developing **Chatty v3**, and it is **WORK IN PROGRESS**. Current branch contains a code of it. 
> You can download latest build in "Actions" (see "Artifacts" section).

Chatty is the modern chat management system for Bukkit-compatible servers. It's based on-top of Kyori's Adventure library, 
that makes it so powerful and stable.

**Key features**:
- Chat channels ("local" and "global" by default)
- Private messaging
- Moderation (CAPS, advertisements, swears)
- Notifications (chat, action bar and title)
- "Vanilla" messages configuring (join/quit/death)
- MiniMessage both legacy (&) styling format

## Building

Chatty uses Gradle to handle dependencies & building. You need JDK 11 or higher to compile Chatty.

### Compiling from source

```shell script
git clone https://github.com/Brikster/Chatty.git
cd Chatty/
./gradlew build
```

Output jar will be placed into `/build/libs` directory.
