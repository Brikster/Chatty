package ru.brikster.chatty.adventure;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public final class NativeBukkitAudienceProvider implements BukkitAudiences {

    @Override
    public @NotNull Audience all() {
        List<Audience> audienceList = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            audienceList.add(new NativeAudienceAdapter(player));
        }
        audienceList.add(new NativeAudienceAdapter(Bukkit.getConsoleSender()));
        return Audience.audience(audienceList);
    }

    @Override
    public @NotNull Audience console() {
        return new NativeAudienceAdapter(Bukkit.getConsoleSender());
    }

    @Override
    public @NotNull Audience players() {
        List<Audience> audienceList = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            audienceList.add(new NativeAudienceAdapter(player));
        }
        return Audience.audience(audienceList);
    }

    @Override
    public @NotNull Audience player(@NotNull UUID playerId) {
        Player player = Bukkit.getPlayer(playerId);
        return player == null ? Audience.empty() : new NativeAudienceAdapter(player);
    }

    @Override
    public @NotNull Audience permission(@NotNull String permission) {
        List<Audience> audienceList = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                audienceList.add(new NativeAudienceAdapter(player));
            }
        }
        audienceList.add(new NativeAudienceAdapter(Bukkit.getConsoleSender()));
        return Audience.audience(audienceList);
    }

    @Override
    public @NotNull Audience world(@NotNull Key world) {
        List<Audience> audienceList = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getWorld().getName().equals(world.asString())) {
                audienceList.add(new NativeAudienceAdapter(player));
            }
        }
        return Audience.audience(audienceList);
    }

    @Override
    public @NotNull Audience server(@NotNull String serverName) {
        return Audience.empty();
    }

    @Override
    public @NotNull ComponentFlattener flattener() {
        return ComponentFlattener.basic();
    }

    @Override
    public void close() {

    }

    @Override
    public @NotNull Audience sender(@NotNull CommandSender sender) {
        return Audience.audience(new NativeAudienceAdapter(sender));
    }

    @Override
    public @NotNull Audience player(@NotNull Player player) {
        return sender(player);
    }

    @Override
    public @NotNull Audience filter(@NotNull Predicate<CommandSender> filter) {
        List<Audience> audienceList = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (filter.test(player)) {
                audienceList.add(new NativeAudienceAdapter(player));
            }
        }
        if (filter.test(Bukkit.getConsoleSender())) {
            audienceList.add(new NativeAudienceAdapter(Bukkit.getConsoleSender()));
        }
        return Audience.audience(audienceList);
    }

}
