#!/usr/bin/env bash
#
# End-to-end smoke test for Chatty.
#
# Boots real Minecraft servers with the built plugin and verifies:
#   A. fresh install       -> the plugin enables, generates configs, and
#                             processes real in-game chat (two bots) cleanly
#   B. legacy v2 migration -> a v2 config.yml is migrated into v3 files
#   C. legacy server       -> the plugin enables and processes chat on an old
#                             server (1.8.8), exercising the bundled Adventure
#                             path and the isolated SQLite driver
#   D. DiscordSRV coexist  -> Chatty and DiscordSRV initialise cleanly side by
#                             side, with no classloader or dependency clash
#
# Requirements: bash, curl, python3, and a JDK the target server accepts
# (point JAVA_HOME at it: 21 for 1.21.x, 11 for 1.16.5, 25 for 26.x).
# The in-game chat test additionally needs node + npm; without them it (and
# scenario C) is skipped. Scenario C also needs a Java 11 runtime for the old
# server — it is downloaded automatically when not supplied via LEGACY_JAVA_HOME.
#
# Build the plugin first (./gradlew build); the workflow does this for CI.
#
# Usage:  JAVA_HOME=/path/to/jdk-21 bash scripts/smoke-test.sh
#
set -euo pipefail

MC_VERSION="${MC_VERSION:-1.21.4}"
LEGACY_MC_VERSION="${LEGACY_MC_VERSION:-1.8.8}"
# Set CHAT_TEST=0 for a server the bot cannot join (mineflayer stops at 1.21.9),
# and LEGACY_SCENARIO=0 to skip the 1.8.8 lane when a matrix covers it elsewhere.
CHAT_TEST="${CHAT_TEST:-1}"
LEGACY_SCENARIO="${LEGACY_SCENARIO:-1}"
CHAT_TEST_SKIP_REASON="disabled with CHAT_TEST=0"
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
# Kept under build/ (git-ignored) so logs survive for inspection / CI artifacts.
WORK="$ROOT/build/smoke-test"
BOT_TOOLS="$ROOT/build/bot-tools"   # cached node_modules for the chat test
JDK_TOOLS="$ROOT/build/jdk-tools"   # cached Java 11 runtime for the legacy server
SERVER="$WORK/server"
LEGACY_SERVER="$WORK/legacy-server"
LEGACY_JAVA_BIN=""
SERVER_PID=""
HOLDER_PID=""

JAVA_BIN="java"
[ -n "${JAVA_HOME:-}" ] && JAVA_BIN="$JAVA_HOME/bin/java"

cleanup() {
    [ -n "$SERVER_PID" ] && kill -9 "$SERVER_PID" 2>/dev/null || true
    [ -n "$HOLDER_PID" ] && kill "$HOLDER_PID" 2>/dev/null || true
}
trap cleanup EXIT

rm -rf "$WORK"
mkdir -p "$WORK"

step() { printf '\n\033[1m=== %s ===\033[0m\n' "$1"; }
fail() { printf '\n\033[31m✗ SMOKE TEST FAILED: %s\033[0m\n' "$*" >&2; exit 1; }

# PaperMC asks API clients to identify themselves with a descriptive agent.
PAPER_USER_AGENT="User-Agent: Chatty-smoke-test (+https://github.com/Brikster/Chatty)"

# Downloads the latest Paper build for a version.  $1 = version, $2 = output jar.
# Uses the v3 ("fill") API — the v2 API was sunset and now answers 410 Gone.
# Builds come back newest first; prefer a stable one, falling back to the
# newest of any channel for versions that have no stable build yet.
download_paper() {
    local version="$1" out="$2" build url
    build="$(curl -fsSL -H "$PAPER_USER_AGENT" "https://fill.papermc.io/v3/projects/paper/versions/$version/builds" \
        | python3 -c 'import sys, json; builds = json.load(sys.stdin); build = next((b for b in builds if b["channel"] == "STABLE"), builds[0]); print(build["id"], build["downloads"]["server:default"]["url"])')"
    url="${build#* }"
    build="${build%% *}"
    curl -fsSL -H "$PAPER_USER_AGENT" -o "$out" "$url"
    echo "Paper $version build $build"
}

# Downloads the latest DiscordSRV release jar.  $1 = output jar.
# Returns non-zero if it cannot be fetched (the scenario is then skipped).
download_discordsrv() {
    local out="$1" url
    url="$(curl -fsSL "https://api.github.com/repos/DiscordSRV/DiscordSRV/releases/latest" \
        | python3 -c 'import sys, json; print(next(a["browser_download_url"] for a in json.load(sys.stdin)["assets"] if a["name"].endswith(".jar")))')" || return 1
    [ -n "$url" ] || return 1
    curl -fsSL -o "$out" "$url" || return 1
    echo "DiscordSRV: ${url##*/}"
}

# Locates a Java 11 runtime for the legacy server — modern JDKs cannot run it.
# Uses $LEGACY_JAVA_HOME when set, otherwise downloads a Temurin JRE 11 under
# build/. Sets LEGACY_JAVA_BIN; returns non-zero when none can be obtained.
ensure_legacy_java() {
    if [ -n "${LEGACY_JAVA_HOME:-}" ] && [ -x "$LEGACY_JAVA_HOME/bin/java" ]; then
        LEGACY_JAVA_BIN="$LEGACY_JAVA_HOME/bin/java"
        return 0
    fi
    local os arch
    case "$(uname -s)" in Darwin) os=mac;; Linux) os=linux;; *) return 1;; esac
    case "$(uname -m)" in arm64 | aarch64) arch=aarch64;; x86_64 | amd64) arch=x64;; *) return 1;; esac
    local dir="$JDK_TOOLS/temurin-jre-11-$os-$arch"
    LEGACY_JAVA_BIN="$(find "$dir" -name java -path '*/bin/*' 2>/dev/null | head -1)"
    if [ -z "$LEGACY_JAVA_BIN" ]; then
        step "Downloading Temurin JRE 11 (to run the legacy $LEGACY_MC_VERSION server)"
        mkdir -p "$dir"
        curl -fsSL "https://api.adoptium.net/v3/binary/latest/11/ga/$os/$arch/jre/hotspot/normal/eclipse" \
            | tar -xz -C "$dir" --strip-components=1 || return 1
        LEGACY_JAVA_BIN="$(find "$dir" -name java -path '*/bin/*' 2>/dev/null | head -1)"
    fi
    [ -n "$LEGACY_JAVA_BIN" ] && [ -x "$LEGACY_JAVA_BIN" ]
}

# --- locate the plugin jar -------------------------------------------------

JAR="$(ls -t "$ROOT"/build/libs/Chatty-*.jar 2>/dev/null | head -1 || true)"
[ -n "$JAR" ] || fail "plugin jar not found in build/libs — run ./gradlew build first"
echo "Plugin jar: $JAR"
"$JAVA_BIN" -version 2>&1 | head -1

# The in-game chat test needs Node.js. Use the system one, or fetch a local
# copy under build/ so the test runs anywhere without a system install.
if [ "$CHAT_TEST" -eq 1 ] && (! command -v node >/dev/null 2>&1 || ! command -v npm >/dev/null 2>&1); then
    node_os=""; node_arch=""
    case "$(uname -s)" in Darwin) node_os=darwin;; Linux) node_os=linux;; esac
    case "$(uname -m)" in arm64 | aarch64) node_arch=arm64;; x86_64 | amd64) node_arch=x64;; esac
    if [ -n "$node_os" ] && [ -n "$node_arch" ]; then
        NODE_VERSION=20.18.1
        NODE_DIR="$BOT_TOOLS/node-v$NODE_VERSION-$node_os-$node_arch"
        if [ ! -x "$NODE_DIR/bin/node" ]; then
            step "Downloading Node.js $NODE_VERSION (for the in-game chat test)"
            mkdir -p "$BOT_TOOLS"
            curl -fsSL "https://nodejs.org/dist/v$NODE_VERSION/node-v$NODE_VERSION-$node_os-$node_arch.tar.gz" \
                | tar -xz -C "$BOT_TOOLS"
        fi
        export PATH="$NODE_DIR/bin:$PATH"
    fi
fi
if [ "$CHAT_TEST" -eq 1 ] && ! command -v node >/dev/null 2>&1; then
    CHAT_TEST=0
    CHAT_TEST_SKIP_REASON="node/npm not available"
    echo "Node.js unavailable — the in-game chat test will be skipped"
fi
if [ "$CHAT_TEST" -eq 0 ]; then
    echo "In-game chat test disabled (CHAT_TEST=0)"
fi

# --- download Paper --------------------------------------------------------

step "Downloading Paper $MC_VERSION"
PAPER_JAR="$WORK/paper.jar"
download_paper "$MC_VERSION" "$PAPER_JAR"

# --- server scaffolding ----------------------------------------------------

mkdir -p "$SERVER/plugins"
echo "eula=true" > "$SERVER/eula.txt"
cat > "$SERVER/server.properties" <<'EOF'
online-mode=false
level-type=flat
spawn-protection=0
max-players=10
EOF
cp "$JAR" "$SERVER/plugins/Chatty.jar"

# Computes the offline-mode UUID of a username (UUID.nameUUIDFromBytes).
offline_uuid() {
    node -e 'const c=require("crypto");const h=c.createHash("md5").update("OfflinePlayer:"+process.argv[1]).digest();h[6]=(h[6]&0x0f)|0x30;h[8]=(h[8]&0x3f)|0x80;const x=h.toString("hex");console.log(`${x.slice(0,8)}-${x.slice(8,12)}-${x.slice(12,16)}-${x.slice(16,20)}-${x.slice(20)}`);' "$1"
}

if [ "$CHAT_TEST" -eq 1 ]; then
    # OP the test bots so they have chatty.* permissions (mentions etc.).
    cat > "$SERVER/ops.json" <<EOF
[
  {"uuid":"$(offline_uuid SmokeSender)","name":"SmokeSender","level":4,"bypassesPlayerLimit":false},
  {"uuid":"$(offline_uuid SmokeTarget)","name":"SmokeTarget","level":4,"bypassesPlayerLimit":false}
]
EOF
    if [ ! -d "$BOT_TOOLS/node_modules/mineflayer" ]; then
        step "Installing mineflayer (for the chat test)"
        mkdir -p "$BOT_TOOLS"
        (cd "$BOT_TOOLS" && npm install --no-fund --no-audit --loglevel=error mineflayer >/dev/null)
    fi
fi

# Boots the server and waits for full startup, leaving it running.
# $1 = path to write the server log to.
start_server() {
    local logfile="$1"
    local pipe="$WORK/stdin.pipe"
    rm -f "$pipe"; mkfifo "$pipe"
    sleep 1800 > "$pipe" &          # holds the stdin pipe open
    HOLDER_PID=$!
    disown "$HOLDER_PID" 2>/dev/null || true
    ( cd "$SERVER" && exec "$JAVA_BIN" -Xmx1G -jar "$PAPER_JAR" nogui ) \
        < "$pipe" > "$logfile" 2>&1 &
    SERVER_PID=$!

    local ready=0 i
    for ((i = 0; i < 240; i++)); do
        if grep -q 'Done (' "$logfile" 2>/dev/null; then ready=1; break; fi
        kill -0 "$SERVER_PID" 2>/dev/null || break
        sleep 1
    done
    [ "$ready" -eq 1 ] || { tail -40 "$logfile" >&2; fail "server did not finish startup"; }
}

# Stops the running server cleanly.
stop_server() {
    local pipe="$WORK/stdin.pipe"
    echo "stop" > "$pipe" 2>/dev/null || true
    local i
    for ((i = 0; i < 60; i++)); do
        kill -0 "$SERVER_PID" 2>/dev/null || break
        sleep 1
    done
    kill -9 "$SERVER_PID" 2>/dev/null || true
    wait "$SERVER_PID" 2>/dev/null || true
    kill "$HOLDER_PID" 2>/dev/null || true
    SERVER_PID=""; HOLDER_PID=""
}

# Verifies a plugin enabled without a fatal error. $1 = plugin name, $2 = log.
assert_plugin_enabled() {
    local name="$1" logfile="$2"
    grep -q "Enabling $name" "$logfile" || { tail -40 "$logfile" >&2; fail "$name was not enabled"; }
    if grep -qE "Error occurred while enabling $name|Could not load .plugins.$name" "$logfile"; then
        grep -nE "$name|Exception|SEVERE" "$logfile" | tail -40 >&2
        fail "$name failed to enable"
    fi
}

# Verifies Chatty enabled without errors. $1 = server log.
assert_enabled() { assert_plugin_enabled "Chatty" "$1"; }

# Portable replacement for `timeout`, which is absent on macOS.
run_with_timeout() {
    local seconds="$1"; shift
    "$@" &
    local pid=$! i
    for ((i = 0; i < seconds; i++)); do
        if ! kill -0 "$pid" 2>/dev/null; then
            if wait "$pid"; then return 0; else return $?; fi
        fi
        sleep 1
    done
    kill -9 "$pid" 2>/dev/null || true
    wait "$pid" 2>/dev/null || true
    return 124
}

# Connects two bots and sends real chat through the plugin. $1 = server log.
run_chat_test() {
    local logfile="$1"
    step "Sending in-game chat through the plugin"
    if ! run_with_timeout 200 env \
            NODE_PATH="$BOT_TOOLS/node_modules" BOT_HOST=127.0.0.1 BOT_PORT=25565 \
            node "$ROOT/scripts/chat-test.js"; then
        tail -40 "$logfile" >&2
        fail "in-game chat test failed"
    fi
    if grep -q "Cannot handle chat event" "$logfile"; then
        grep -nE "Cannot handle chat event|Exception" "$logfile" | tail -20 >&2
        fail "Chatty logged a chat-processing error while bots were chatting"
    fi
    echo "✓ chat pipeline processed real in-game messages without errors"
}

# --- scenario A: fresh install + live chat ---------------------------------

step "Scenario A — fresh install"
rm -rf "$SERVER/plugins/Chatty" "$SERVER"/plugins/Chatty_old_*
FRESH_LOG="$WORK/fresh.log"
start_server "$FRESH_LOG"
assert_enabled "$FRESH_LOG"
[ -f "$SERVER/plugins/Chatty/settings.yml" ]     || fail "settings.yml was not generated"
[ -f "$SERVER/plugins/Chatty/chats.yml" ]        || fail "chats.yml was not generated"
[ -f "$SERVER/plugins/Chatty/lang/en-US.yml" ]   || fail "lang/en-US.yml was not generated"
[ -f "$SERVER/plugins/Chatty/lang/ru-RU.yml" ]   || fail "bundled lang/ru-RU.yml was not copied"
grep -q "Игрок" "$SERVER/plugins/Chatty/lang/ru-RU.yml" || fail "lang/ru-RU.yml has no Russian content"
echo "✓ plugin enables and generates config (incl. lang files) on a fresh install"
if [ "$CHAT_TEST" -eq 1 ]; then
    run_chat_test "$FRESH_LOG"
else
    echo "• in-game chat test skipped ($CHAT_TEST_SKIP_REASON)"
fi
stop_server

# --- scenario B: legacy v2 migration ---------------------------------------

step "Scenario B — legacy v2 migration"
rm -rf "$SERVER/plugins/Chatty" "$SERVER"/plugins/Chatty_old_*
mkdir -p "$SERVER/plugins/Chatty"
cp "$ROOT/scripts/fixtures/v2-config.yml" "$SERVER/plugins/Chatty/config.yml"
MIGRATE_LOG="$WORK/migrate.log"
start_server "$MIGRATE_LOG"
assert_enabled "$MIGRATE_LOG"

grep -q "Migrating legacy Chatty v2 configuration" "$MIGRATE_LOG" || fail "migration did not start"
grep -q "Legacy configuration migrated"           "$MIGRATE_LOG" || fail "migration did not finish"
! grep -q "Failed to migrate legacy configuration" "$MIGRATE_LOG" || fail "migration threw an exception"
! grep -q "could not be migrated automatically"    "$MIGRATE_LOG" || fail "a config file failed to migrate"

ls -d "$SERVER"/plugins/Chatty_old_* >/dev/null 2>&1 || fail "v2 backup folder was not created"

CHATS="$SERVER/plugins/Chatty/chats.yml"
SETTINGS="$SERVER/plugins/Chatty/settings.yml"
MODERATION="$SERVER/plugins/Chatty/moderation.yml"
PM="$SERVER/plugins/Chatty/pm.yml"

# These also prove okaeri reloaded the migrated YAML without rejecting it:
# the values survive the migrator write -> okaeri load -> okaeri re-save.
grep -q "MIGRATED_MARKER" "$CHATS"     || fail "chat format was not migrated into chats.yml"
grep -q "HIGHEST"         "$SETTINGS"  || fail "listener-priority was not migrated"
grep -q "IP_PATTERN_X"    "$MODERATION" || fail "advertisement ip pattern was not migrated"
grep -q "from-name"       "$PM"        || fail "PM format placeholders were not migrated"
if grep -q "disabled_chat" "$CHATS"; then fail "a disabled v2 chat was migrated"; fi

echo "✓ legacy v2 config migrated and reloaded successfully"
stop_server

echo
grep -E "\[Chatty\].*(Migrat|migrat|review|-)" "$MIGRATE_LOG" || true

# --- scenario D: DiscordSRV coexistence ------------------------------------

step "Scenario D — DiscordSRV coexistence"
if download_discordsrv "$WORK/DiscordSRV.jar"; then
    rm -rf "$SERVER/plugins/Chatty" "$SERVER"/plugins/Chatty_old_* "$SERVER/plugins/DiscordSRV"
    cp "$JAR" "$SERVER/plugins/Chatty.jar"
    cp "$WORK/DiscordSRV.jar" "$SERVER/plugins/DiscordSRV.jar"
    # A syntactically valid but fake bot token makes DiscordSRV run its full
    # init (config, listeners) before stopping at the Discord connection step.
    # DiscordSRV shuts itself down on any token that is not a real one, so it
    # cannot stay enabled in CI; the test therefore verifies the two plugins
    # initialise cleanly side by side — no classloader/dependency clash and
    # neither breaks the other's startup — which is all that is checkable
    # without a real Discord bot token.
    mkdir -p "$SERVER/plugins/DiscordSRV"
    cat > "$SERVER/plugins/DiscordSRV/config.yml" <<'EOF'
BotToken: "MTI3NzAwMDAwMDAwMDAwMDAwMA.GfABCD.thisIsAFakeDiscordSrvSmokeTestTokenNotReal"
EOF
    COEXIST_LOG="$WORK/discordsrv.log"
    start_server "$COEXIST_LOG"

    # Both plugins' onEnable runs synchronously during startup, so once the
    # server is up each has either finished or logged a failure. DiscordSRV
    # then shuts itself down asynchronously because the fake token cannot
    # connect to Discord — that is expected and follows a full, clean init.
    assert_plugin_enabled "Chatty" "$COEXIST_LOG"
    assert_plugin_enabled "DiscordSRV" "$COEXIST_LOG"
    if grep -qE "NoClassDefFoundError|NoSuchMethodError|LinkageError|IncompatibleClassChangeError" "$COEXIST_LOG"; then
        grep -nE "NoClassDefFoundError|NoSuchMethodError|LinkageError|IncompatibleClassChangeError" "$COEXIST_LOG" | tail -20 >&2
        fail "classloader/dependency clash with Chatty and DiscordSRV installed together"
    fi
    echo "✓ Chatty and DiscordSRV initialise cleanly side by side (no classloader clash)"
    if [ "$CHAT_TEST" -eq 1 ]; then
        run_chat_test "$COEXIST_LOG"
    else
        echo "• in-game chat test skipped ($CHAT_TEST_SKIP_REASON)"
    fi
    stop_server
    rm -f "$SERVER/plugins/DiscordSRV.jar"
else
    echo "• DiscordSRV scenario skipped (could not download DiscordSRV)"
fi

# --- scenario C: legacy server ---------------------------------------------

step "Scenario C — legacy server ($LEGACY_MC_VERSION)"
if [ "$LEGACY_SCENARIO" -eq 1 ] && [ "$CHAT_TEST" -eq 1 ] && ensure_legacy_java; then
    "$LEGACY_JAVA_BIN" -version 2>&1 | head -1
    download_paper "$LEGACY_MC_VERSION" "$WORK/paper-legacy.jar"

    rm -rf "$LEGACY_SERVER"
    mkdir -p "$LEGACY_SERVER/plugins"
    echo "eula=true" > "$LEGACY_SERVER/eula.txt"
    # use-native-transport=false forces the NIO transport: the ancient Netty
    # epoll bundled with 1.8.8 crashes ("Unable to access address of buffer")
    # on modern Linux kernels, e.g. CI runners.
    cat > "$LEGACY_SERVER/server.properties" <<'EOF'
online-mode=false
level-type=flat
spawn-protection=0
max-players=10
use-native-transport=false
EOF
    cp "$JAR" "$LEGACY_SERVER/plugins/Chatty.jar"
    cat > "$LEGACY_SERVER/ops.json" <<EOF
[
  {"uuid":"$(offline_uuid SmokeSender)","name":"SmokeSender","level":4,"bypassesPlayerLimit":false},
  {"uuid":"$(offline_uuid SmokeTarget)","name":"SmokeTarget","level":4,"bypassesPlayerLimit":false}
]
EOF

    # The legacy server runs on its own (older) Java and Paper jar; start_server
    # and stop_server act on these globals.
    SERVER="$LEGACY_SERVER"
    PAPER_JAR="$WORK/paper-legacy.jar"
    JAVA_BIN="$LEGACY_JAVA_BIN"
    LEGACY_LOG="$WORK/legacy.log"
    start_server "$LEGACY_LOG"
    assert_enabled "$LEGACY_LOG"
    echo "✓ plugin enables on a legacy $LEGACY_MC_VERSION server (bundled Adventure + isolated SQLite driver)"
    run_chat_test "$LEGACY_LOG"
    stop_server
else
    echo "• legacy-server scenario skipped (disabled, or node/Java 11 unavailable)"
fi

printf '\n\033[32m✓ SMOKE TEST PASSED\033[0m\n'
