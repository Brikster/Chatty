package ru.brikster.chatty.notification.advancement;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.brikster.chatty.util.SchedulerUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
public final class AdvancementToaster {

    private static final String KEY_PREFIX = "notification/";
    private static final String ROOT_KEY = KEY_PREFIX + "root";
    private static final long REVOKE_DELAY_TICKS = 2L;

    @Inject private Plugin plugin;
    @Inject private Logger logger;

    private final Set<NamespacedKey> registered = new LinkedHashSet<>();

    public @Nullable Advancement register(@NotNull String id,
                                          @NotNull Component title,
                                          @NotNull Component subtitle,
                                          @NotNull String icon,
                                          @NotNull AdvancementFrame frame) {
        NamespacedKey root = registerRoot();

        Component heading = AdvancementJson.heading(title, subtitle);
        NamespacedKey key = new NamespacedKey(plugin,
                KEY_PREFIX + AdvancementJson.sanitize(id)
                        + "_" + AdvancementJson.fingerprint(heading, icon, frame));

        Advancement existing = Bukkit.getAdvancement(key);
        if (existing != null) {
            registered.add(key);
            return existing;
        }

        Throwable modernFailure;
        try {
            Advancement advancement = Bukkit.getUnsafe()
                    .loadAdvancement(key,
                            AdvancementJson.toast(root.toString(), heading, icon, frame, true));
            registered.add(key);
            return advancement;
        } catch (Throwable t) {
            modernFailure = t;
        }

        try {
            Advancement advancement = Bukkit.getUnsafe()
                    .loadAdvancement(key,
                            AdvancementJson.toast(root.toString(), heading, icon, frame, false));
            registered.add(key);
            return advancement;
        } catch (Throwable legacyFailure) {
            logger.log(Level.WARNING, "Cannot register the toast notification \"" + id
                    + "\". Check that \"icon\" names an item this server knows,"
                    + " for example minecraft:diamond.", modernFailure);
            return null;
        }
    }

    public void show(@NotNull Player player, @NotNull Advancement advancement) {
        SchedulerUtil.runForPlayer(plugin, player, () -> {
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            if (progress.getAwardedCriteria().contains(AdvancementJson.CRITERION)) {
                return;
            }
            progress.awardCriteria(AdvancementJson.CRITERION);
            SchedulerUtil.runForPlayerLater(plugin, player,
                    () -> player.getAdvancementProgress(advancement).revokeCriteria(AdvancementJson.CRITERION),
                    REVOKE_DELAY_TICKS);
        });
    }

    /**
     * Drops the datapack entry of every toast this run no longer uses, so
     * editing a notification does not leave its old file behind.
     *
     * <p>Two sources are consulted. Advancements the running server still
     * knows come from its registry; those written by earlier runs do not,
     * because Bukkit writes them into a "advancements" directory that modern
     * servers no longer read back, so the keys are also kept in a file of our
     * own. The advancement itself survives in a running server's registry -
     * Bukkit cannot take it out without a full data reload - but nothing
     * awards it, so nobody sees it again.
     */
    public void removeStale() {
        Set<NamespacedKey> stale = new LinkedHashSet<>(readPreviousKeys());

        for (Iterator<Advancement> iterator = Bukkit.advancementIterator(); iterator.hasNext(); ) {
            NamespacedKey key = iterator.next().getKey();
            if (key.getNamespace().equals(ownNamespace()) && key.getKey().startsWith(KEY_PREFIX)) {
                stale.add(key);
            }
        }
        stale.removeAll(registered);

        for (NamespacedKey key : stale) {
            try {
                Bukkit.getUnsafe().removeAdvancement(key);
            } catch (Throwable t) {
                logger.log(Level.FINE, "Cannot drop the unused toast " + key, t);
            }
        }

        writeCurrentKeys();
    }

    private Path keysFile() {
        return plugin.getDataFolder().toPath().resolve("advancement-keys.txt");
    }

    private List<NamespacedKey> readPreviousKeys() {
        Path file = keysFile();
        if (!Files.isReadable(file)) {
            return Collections.emptyList();
        }

        List<NamespacedKey> keys = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                String trimmed = line.trim();
                int separator = trimmed.indexOf(':');
                if (separator > 0) {
                    keys.add(new NamespacedKey(trimmed.substring(0, separator),
                            trimmed.substring(separator + 1)));
                }
            }
        } catch (Throwable t) {
            logger.log(Level.FINE, "Cannot read " + file, t);
        }
        return keys;
    }

    private void writeCurrentKeys() {
        Path file = keysFile();
        try {
            if (registered.isEmpty()) {
                Files.deleteIfExists(file);
                return;
            }
            Files.createDirectories(file.getParent());
            List<String> lines = new ArrayList<>();
            for (NamespacedKey key : registered) {
                lines.add(key.toString());
            }
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (Throwable t) {
            logger.log(Level.FINE, "Cannot write " + file, t);
        }
    }

    private String ownNamespace() {
        return new NamespacedKey(plugin, "x").getNamespace();
    }

    private NamespacedKey registerRoot() {
        NamespacedKey key = new NamespacedKey(plugin, ROOT_KEY);
        registered.add(key);

        if (Bukkit.getAdvancement(key) == null) {
            try {
                Bukkit.getUnsafe().loadAdvancement(key, AdvancementJson.root());
            } catch (Throwable t) {
                logger.log(Level.FINE, "Cannot register the toast root " + key, t);
            }
        }
        return key;
    }


}
