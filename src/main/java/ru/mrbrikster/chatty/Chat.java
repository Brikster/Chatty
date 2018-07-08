package ru.mrbrikster.chatty;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;

public class Chat {

    @Getter private final String name;
    @Getter private final boolean enable;
    @Getter private final String format;
    @Getter private final int range;
    @Getter private final String symbol;
    @Getter private final boolean permission;
    @Getter private final long cooldown;

    Chat(String name, boolean enable, String format, int range, String symbol, boolean permission, long cooldown) {
        this.name = name.toLowerCase();
        this.enable = enable;
        this.format = format;
        this.range = range;
        this.symbol = symbol == null ? "" : symbol;
        this.permission = permission;
        this.cooldown = cooldown * 1000;
    }

    public void setCooldown(Main main, Player player) {
        player.setMetadata("chatty.cooldown." + this.name, new FixedMetadataValue(main, System.currentTimeMillis()));
    }

    public long getCooldown(Player player) {
        List<MetadataValue> metadataValues = player.getMetadata("chatty.cooldown." + this.name);

        if (metadataValues.isEmpty())
            return -1;

        long cooldown = (metadataValues.get(0).asLong() + this.cooldown - System.currentTimeMillis()) / 1000;
        return cooldown > 0 ? cooldown : -1;
    }

    public static class Requirement {

        private final Type type;
        private String name;
        private List<String> lore;
        private int amount;
        private Material material;
        private int money;
        private short durability;

        public enum Type {
            VAULT, ITEM;
        }

        public Requirement(ConfigurationSection configurationSection) {
            String typeName = configurationSection.getString("type");

            this.type = Type.valueOf(typeName.toUpperCase());

            switch (type) {
                case VAULT:
                    this.money = configurationSection.getInt("data");
                    break;
                case ITEM:
                    ConfigurationSection item = configurationSection.getConfigurationSection("data");
                    this.material = Material.valueOf(item.getString("material").toUpperCase());
                    this.amount = item.getInt("amount", 1);
                    this.name = item.getString("name");
                    this.lore = item.getStringList("lore");
                    this.durability = (short) item.getInt("durability", -1);
                    break;
            }
        }

        public boolean check(Main main, Player player) {
            switch (type) {
                case VAULT:
                    return main.getEconomyManager()
                            .withdraw(player, money);
                case ITEM:
                    boolean hasItem = hasItem(player);
                    takeItem(player);
                    return hasItem;
            }

            return false;
        }

        private boolean hasItem(Player player) {
            int amountFound = 0;

            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == material && isValidDataValue(item.getDurability()) && isValidNameAndLore(item.getItemMeta())) {
                    amountFound += item.getAmount();
                }
            }

            return amountFound >= amount;
        }

        private boolean isValidDataValue(short durability) {
            return (durability < 0 || this.durability == durability);
        }

        private void takeItem(Player player) {
            if (amount <= 0) {
                return;
            }

            int itemsToTake = amount;

            ItemStack[] contents = player.getInventory().getContents();
            ItemStack current;


            for (int i = 0; i < contents.length; i++) {
                current = contents[i];

                if (current != null && current.getType() == material && isValidDataValue(current.getDurability()) && isValidNameAndLore(current.getItemMeta())) {
                    if (current.getAmount() > itemsToTake) {
                        current.setAmount(current.getAmount() - itemsToTake);
                        return;
                    } else {
                        itemsToTake -= current.getAmount();
                        player.getInventory().setItem(i, new ItemStack(Material.AIR));
                    }
                }

                if (itemsToTake <= 0) return;
            }
        }

        private boolean isValidNameAndLore(ItemMeta itemMeta) {
            return name == null 
                    || (name.equals(itemMeta.getDisplayName()) &&
                    checkLores(lore, itemMeta.getLore()));
        }

        private boolean checkLores(List<String> lore1, List<String> lore2) {
            String[] arrayLore1 = parseLore(lore1);
            String[] arrayLore2 = parseLore(lore2);

            if (arrayLore1.length != arrayLore2.length) return false;

            for (int i = 0; i < arrayLore1.length; i++) {
                if (!arrayLore1[i].equals(arrayLore2[i]))
                    return false;
            }

            return true;
        }

        private String[] parseLore(List<String> lore) {
            boolean isNull = lore == null;

            String[] result = new String[isNull ? 1 : lore.size()];

            if (isNull) return new String[] {};

            for (int i = 0; i < lore.size(); i++) {
                result[i] = ChatColor.translateAlternateColorCodes('&', lore.get(i));
            }

            return result;
        }

    }

}
