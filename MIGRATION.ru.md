# Переход с Chatty v2 на v3

Chatty v3 переписан с нуля. Конфигурация разделена на несколько файлов
(`settings.yml`, `chats.yml`, `pm.yml`, `moderation.yml`, `vanilla.yml`,
`notifications.yml`, `replacements.yml`, `proxy.yml`) и папку `lang/` вместо
единственного `config.yml` из v2.

> English version: [MIGRATION.md](MIGRATION.md)

## Автоматический перенос

Если при запуске v3 находит в `plugins/Chatty/` старый `config.yml` от v2, он:

1. Переименовывает всю папку в `Chatty_old_<timestamp>/` — старая конфигурация
   никогда не удаляется.
2. Создаёт свежие файлы конфигурации v3.
3. Переносит в них всё, что можно сопоставить однозначно.
4. Выводит в лог список того, что требует ручного внимания.

**Переносится автоматически:**

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

Примечания:
- `range: -3` (кросс-серверный чат) сохраняется. В v2 такие чаты шли через
  BungeeCord, в v3 — через Redis, поэтому настройте `proxy.yml`.
- `cooldown: -1` (отключён) становится `0`.
- Чаты, отключённые в v2 (`enable: false`), не переносятся.
- Плейсхолдеры формата личных сообщений переименованы: `{sender-*}` → `{from-*}`,
  `{recipient-*}` → `{to-*}`.
- В формате упоминаний `{player}` заменяется на `{username}`.
- Значения `caps.length` и `caps.percent` приводятся к допустимому диапазону:
  v2 позволял `length: 0` и `percent` больше 100, v3 такие значения отвергает.

## Требует ручной настройки

Это **не** переносится автоматически и остаётся со значениями v3 по умолчанию:

- **Звуки** — в v2 использовались имена звуков Bukkit (`CLICK`, `ORB_PICKUP`),
  в v3 — ключи Adventure. Переустановите параметры `sound`, где нужно.
- **Интерактивные замены** (`json.replacements`) — в v3 более простой формат
  MiniMessage в `replacements.yml`.
- **Автосообщения** (`notifications.*`) — создайте заново в `notifications.yml`.
- **Тексты локализации** — v2 `locale/*.yml` → v3 `lang/<язык>.yml`. Сам код
  языка из `general.locale` переносится в `settings.yml` → `language`
  (`ru` → `ru-RU`, `zh_CN` → `zh-CN` и так далее); нераспознанное значение
  указывается в примечаниях при запуске, а язык остаётся по умолчанию.
- **Пер-чатовые переключатели модерации** — прямого эквивалента в v3 нет.
- **Кросс-серверный чат** — в v3 через Redis, настраивается в `proxy.yml`.

## Права

Большинство узлов из v2 продолжают работать: v3 проверяет и старое написание,
и новое, поэтому перечисленные ниже группы менять не нужно.

| Узел v2 | Узел v3 | Состояние |
| --- | --- | --- |
| `chatty.chat.<chat>.see` | `chatty.chat.<chat>.read` | принимаются оба |
| `chatty.cooldown.<chat>` | `chatty.bypass.cooldown.<chat>` | принимаются оба |
| `chatty.moderation.caps` | `chatty.bypass.moderation.caps` | принимаются оба |
| `chatty.moderation.swear` | `chatty.bypass.moderation.swear` | принимаются оба |
| `chatty.moderation.advertisement` | `chatty.bypass.moderation.ads` | принимаются оба |
| `chatty.command.msg` | `chatty.pm` | принимаются оба |
| `chatty.command.reply` | `chatty.pm` | принимаются оба |
| `chatty.notification.actionbar` | `chatty.notification.actionbar.<name>` | принимаются оба |

### Нужно выдать заново

`chatty.style.*` больше не значит то, что значил раньше. В v2 он разрешал
игроку *писать* цветовые коды и форматирование; в v3 такой же по виду узел
выбирает именованный формат чата из `chats.yml`, а возможность писать коды
переехала в `chatty.decoration.*`.

**Уберите свои маски `chatty.style.*`** — оставленные на месте, они теперь
подходят под каждый именованный стиль из `chats.yml`, включая те, которые вы
собирались продавать, — и выдайте замену явно:

| Узел v2 | Узел v3 |
| --- | --- |
| `chatty.style.colors` | `chatty.decoration.color` |
| `chatty.style.bold` | `chatty.decoration.bold` |
| `chatty.style.italic` | `chatty.decoration.italic` |
| `chatty.style.underline` | `chatty.decoration.underlined` |
| `chatty.style.strikethrough` | `chatty.decoration.strikethrough` |
| `chatty.style.magic` | `chatty.decoration.obfuscated` |
| `chatty.style.reset` | `chatty.decoration.reset` |
| `chatty.spy` | `chatty.spy.pm` |

Пер-чатовая форма из v2 `chatty.style.<style>.<chat>` имеет эквивалент — он
пишется в пространстве самого чата: `chatty.chat.<чат>.decoration.<стиль>`.
То есть `chatty.style.colors.global` становится
`chatty.chat.global.decoration.color`, а `chatty.decoration.color` без чата
по-прежнему действует на все чаты. Чат нельзя поставить в конец, как было в v2:
эта позиция уже занята названием цвета — `chatty.decoration.color.red` означает
«можно красный».

Узел `chatty.decoration` сам по себе даёт ещё и hex-цвета, которых v2 в
сообщениях игроков не допускал — выдавайте отдельные листья, если это не то,
что вам нужно.

### Исчезло вместе с функцией

У `chatty.command.chat`, `chatty.command.prefix[.others]`,
`chatty.command.suffix[.others]`, `chatty.command.swears` и
`chatty.swears.see` эквивалента в v3 нет.

Всплывающие уведомления вернулись, но узел переименован: вместо
`chatty.notification.advancements.<name>` из v2 —
`chatty.notification.advancement.<name>`, а сами каналы настраиваются в
`notifications.yml` в секции `advancements`.

### Про /clearchat

`/clearchat` очищает чат только вам, как и в v2. Очистка для всех — это
`/clearchat all`, и она требует `chatty.command.clearchat.all`.

## Требования

- **Java 11 или новее.** На Java 8 плагин не запустится.
- **Minecraft 1.8.8 или новее.**

Если сервер этим требованиям не соответствует, оставайтесь на версии 2.19.14.

## Советы

- v3 по-прежнему понимает старые цветовые коды с `&`, а также MiniMessage и
  hex-форматы — ваши форматы из v2 продолжат работать после переноса.
- Просмотрите перенесённые файлы и лог запуска до того, как открывать сервер.
- Сохраняйте папку `Chatty_old_<timestamp>/`, пока не убедитесь, что всё в
  порядке.
- Отправить сообщение в конкретный чат с консоли или из планировщика можно
  командой `/chatty broadcast <чат> <сообщение>`.
