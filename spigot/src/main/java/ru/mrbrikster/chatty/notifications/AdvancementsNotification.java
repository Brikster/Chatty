package ru.mrbrikster.chatty.notifications;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import ru.mrbrikster.chatty.Chatty;
import ru.mrbrikster.chatty.reflection.Reflection;
import ru.mrbrikster.chatty.util.Debugger;
import ru.mrbrikster.chatty.util.TextUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvancementsNotification extends Notification {

    private static final String PERMISSION_NODE = NOTIFICATION_PERMISSION_NODE + "advancements.%s";
    private final List<Map<?, ?>> messages;
    private final String name;

    AdvancementsNotification(String name, double delay, List<Map<?, ?>> messages, boolean permission, boolean random) {
        super(delay, permission, messages.size(), random);

        this.name = name;
        this.messages = messages;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        if (messages.isEmpty()) {
            return;
        }

        Chatty.instance().getExact(Debugger.class).debug("Run \"%s\" AdvancementsNotification.", name);

        AdvancementMessage advancementMessage = new AdvancementMessage((Map<String, String>) messages.get(nextMessage()));
        Reflection.getOnlinePlayers().stream().filter(player -> !isPermission() || player.hasPermission(String.format(PERMISSION_NODE, name)))
                .forEach(advancementMessage::show);
    }

    private static class AdvancementMessage implements ConfigurationSerializable {

        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

        private final NamespacedKey id;
        private final String icon;
        private final String header;
        private final String footer;
        private final JavaPlugin javaPlugin;

        private AdvancementMessage(Map<String, String> list) {
            this(list.getOrDefault("header", "Header"),
                    list.getOrDefault("footer", "Message #1"),
                    list.getOrDefault("icon", "minecraft:apple"), Chatty.instance());
        }

        AdvancementMessage(String header, String footer, String icon, JavaPlugin javaPlugin) {
            this.header = header;
            this.footer = footer;
            this.icon = icon;
            this.javaPlugin = javaPlugin;

            this.id = new NamespacedKey(javaPlugin, "chatty" + (RANDOM.nextInt(1000000) + 1));
        }

        void show(Player player) {
            this.register();

            this.grant(player);

            Bukkit.getScheduler().runTaskLater(javaPlugin, () -> {
                revoke(player);
                unregister();
            }, 20);
        }

        @SuppressWarnings("deprecation")
        private void register() {
            try {
                Bukkit.getUnsafe().loadAdvancement(id, this.json());
            } catch (IllegalArgumentException ignored) {
            }
        }

        @SuppressWarnings("deprecation")
        private void unregister() {
            Bukkit.getUnsafe().removeAdvancement(id);
        }

        private void grant(Player player) {
            Advancement advancement = Bukkit.getAdvancement(id);
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            if (!progress.isDone()) {
                progress.getRemainingCriteria().forEach(progress::awardCriteria);
            }
        }

        private void revoke(Player player) {
            Advancement advancement = Bukkit.getAdvancement(id);
            AdvancementProgress progress = player.getAdvancementProgress(advancement);
            if (progress.isDone()) {
                progress.getAwardedCriteria().forEach(progress::revokeCriteria);
            }
        }

        private String json() {
            JsonObject json = new JsonObject();

            JsonObject display = new JsonObject();

            JsonObject icon = new JsonObject();
            icon.addProperty("item", this.icon);

            display.add("icon", icon);
            display.addProperty("title", TextUtil.stripHex(TextUtil.stylish(this.header + "\n" + this.footer)));
            display.addProperty("description", "Chatty Announcement");
            display.addProperty("background", "minecraft:textures/gui/advancements/backgrounds/stone.png");
            display.addProperty("frame", "goal");
            display.addProperty("announce_to_chat", false);
            display.addProperty("show_toast", true);
            display.addProperty("hidden", true);

            JsonObject trigger = new JsonObject();
            trigger.addProperty("trigger", "minecraft:impossible");

            JsonObject criteria = new JsonObject();
            criteria.add("impossible", trigger);

            json.add("criteria", criteria);
            json.add("display", display);

            return GSON.toJson(json);
        }

        @Override
        public @NotNull Map<String, Object> serialize() {
            Map<String, Object> map = new HashMap<>();

            map.put("icon", icon);
            map.put("header", header);
            map.put("footer", footer);

            return map;
        }

    }

}
