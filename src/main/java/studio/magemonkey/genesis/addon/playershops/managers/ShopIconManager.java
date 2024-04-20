package studio.magemonkey.genesis.addon.playershops.managers;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.addon.playershops.objects.LimitedNode;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShop;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShopIcon;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShopSimple;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.GenesisShop;
import studio.magemonkey.genesis.managers.ClassManager;

import java.util.ArrayList;
import java.util.List;

public class ShopIconManager {
    private final List<PlayerShopIcon> icons;
    private final boolean              usePlayerHeads, allowIconSelection;
    private final LimitedNode inventoryItem, rename, renameColor;

    private GenesisShop iconSelection;

    public ShopIconManager(FileConfiguration config, FileConfiguration iconslist) {
        usePlayerHeads = config.getBoolean("ShopIcon.UsePlayerHeads");
        allowIconSelection = config.getBoolean("ShopIcon.AllowIconSelection");
        inventoryItem = new LimitedNode(config.getConfigurationSection("ShopIcon.AllowInventoryItem"));
        rename = new LimitedNode(config.getConfigurationSection("ShopIcon.AllowShopRename"));
        renameColor = new LimitedNode(config.getConfigurationSection("ShopIcon.ShopRenameAllowColors"));

        icons = new ArrayList<PlayerShopIcon>();

        for (String key : iconslist.getConfigurationSection("List").getKeys(false)) {
            PlayerShopIcon icon = new PlayerShopIcon(iconslist.getConfigurationSection("List." + key));
            icons.add(icon);

        }

    }

    public void setupIconShop(PlayerShops plugin) {
        if (getAllowIconSelection()) {
            iconSelection = plugin.getShopCreator().createShopIconSelector(plugin, this, true);

            for (int i = icons.size() - 1; i >= 0; i--) {
                PlayerShopIcon icon  = icons.get(i);
                GenesisBuy     buy_a = icon.createShopItemAllow(plugin, icon.getPath());
                iconSelection.addShopItem(buy_a, buy_a.getItem(), ClassManager.manager);
                GenesisBuy buy_d = icon.createShopitemDeny(plugin, icon.getPath());
                iconSelection.addShopItem(buy_d, buy_d.getItem(), ClassManager.manager);
            }

            iconSelection.finishedAddingItems();
        }
    }


    public boolean isShopIconFix() {
        if (usePlayerHeads) {
            return true;
        }
        return !allowIconSelection;
    }

    public GenesisShop getIconSelectionShop() {
        return iconSelection;
    }


    public ItemStack createPlayerheadItem(PlayerShopSimple shop) {
        ItemStack i    = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) i.getItemMeta();
        meta.setOwner(shop.getOwnerName());
        i.setItemMeta(meta);
        return i;
    }

    public PlayerShopIcon getHighestShopIcon(Player p, PlayerShopSimple shop) {
        for (PlayerShopIcon icon : icons) {
            if (icon.canUse(p, shop)) {
                return icon;
            }
        }
        return null;
    }

    public ItemStack transformName(PlayerShops plugin,
                                   PlayerShopSimple shop,
                                   ItemStack i,
                                   boolean use_default_text,
                                   boolean inventoryitem) {
        if (i == null) {
            return null;
        }
        ItemMeta meta = i.getItemMeta();


        String title = meta.getDisplayName();
        if (title == null || use_default_text) {
            title = inventoryitem ? plugin.getMessages().get("ShopIcon.InventoryItemTitle")
                    : plugin.getMessages().get("ShopIcon.DefaultTitle");
        }
        if (shop != null) {
            title = title.replace("%playershopname%", shop.getShopDisplayName());
        }
        meta.setDisplayName(ClassManager.manager.getStringManager().transform(title));

        if (!meta.hasLore() || use_default_text) {
            List<String> lore = new ArrayList<String>();
            String       desc = inventoryitem ? plugin.getMessages().get("ShopIcon.InventoryItemDescription")
                    : plugin.getMessages().get("ShopIcon.DefaultDescription");
            if (shop != null) {
                desc = desc.replace("%playershopname%", shop.getShopDisplayName());
                desc = desc.replace("%player%", shop.getOwnerName());
            }
            lore.add(ClassManager.manager.getStringManager().transform(desc));
            meta.setLore(lore);
        }

        i.setItemMeta(meta);
        return i;
    }

    public ItemStack getHighestShopIconItem(Player p, PlayerShopSimple shop, boolean can_return_null) {
        if (usePlayerHeads) {
            return createPlayerheadItem(shop);
        }
        if (!allowIconSelection) {
            PlayerShopIcon icon = getHighestShopIcon(p, shop);
            if (icon == null) {
                return new ItemStack(Material.DIRT, 1);
            } else {
                return icon.getItem();
            }
        }
        if (can_return_null) {
            return null;
        } else {
            return new ItemStack(Material.DIRT, 1);
        }
    }

    public List<PlayerShopIcon> getIcons() {
        return icons;
    }


    public boolean getUsePlayerHeads() {
        return usePlayerHeads;
    }


    public boolean getAllowIconSelection() {
        if (usePlayerHeads) {
            return false;
        }
        return allowIconSelection;
    }

    public boolean getAllowInventoryItem(Player p, PlayerShop shop) {
        return inventoryItem.meetsRequirements(shop, p);
    }

    public boolean getAllowShopRename(Player p, PlayerShop shop) {
        return rename.meetsRequirements(shop, p);
    }

    public boolean getAllowShopRenameColors(Player p, PlayerShop shop) {
        return renameColor.meetsRequirements(shop, p);
    }


}
