package studio.magemonkey.genesis.addon.playershops.objects;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.conditions.GenesisSingleCondition;
import studio.magemonkey.genesis.core.prices.GenesisPriceType;
import studio.magemonkey.genesis.core.rewards.GenesisRewardType;
import studio.magemonkey.genesis.managers.ClassManager;

import java.util.List;

public class PlayerShopIcon {
    @Getter
    private final String       path;
    private final ItemStack    item;
    @Getter
    private final List<String> itemData;
    private final String       permission;
    private final int          slotNeeded;


    public PlayerShopIcon(ConfigurationSection section) {
        this.path = section.getName();
        itemData = section.getStringList("Icon");
        item = ClassManager.manager.getItemStackCreator().createItemStack(itemData, true);
        permission = section.getString("Permission");
        slotNeeded = section.getInt("SlotsNeeded");
    }


    public boolean canUse(Player p, PlayerShopSimple shop) {
        if (permission != null) {
            if (!permission.isEmpty()) {
                if (p == null) {
                    return false;
                }
                if (!p.hasPermission(permission)) {
                    return false;
                }
            }
        }
        return shop.getSlotsAmount(p, true) >= slotNeeded;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public GenesisBuy createShopItemAllow(PlayerShops plugin, String name) {
        ItemStack item = plugin.getIconManager().transformName(plugin, null, this.item.clone(), false, false);
        GenesisBuy buy = new GenesisBuy(plugin.getGenesisListener().getRewardTypeShopIcon(),
                GenesisPriceType.Nothing,
                item,
                null,
                plugin.getMessages().get("Message.IconSelected"),
                -1,
                permission,
                name,
                new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopSlotsCondition(),
                        "over",
                        String.valueOf(slotNeeded - 1)),
                null,
                null);
        buy.setItem(item, false);
        return buy;
    }

    public GenesisBuy createShopitemDeny(PlayerShops plugin, String name) {
        ItemStack item = plugin.getIconManager().transformName(plugin, null, this.item.clone(), false, false);

        ItemMeta     meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        lore.add(plugin.getMessages().get("ShopIcon.RequiresSlots").replace("%slots%", String.valueOf(slotNeeded)));
        meta.setLore(lore);
        item.setItemMeta(meta);

        GenesisBuy buy = new GenesisBuy(GenesisRewardType.Nothing,
                GenesisPriceType.Nothing,
                null,
                null,
                plugin.getMessages().get("Message.RequiresMoreSlots"),
                -1,
                null,
                name,
                new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopSlotsCondition(),
                        "under",
                        String.valueOf(slotNeeded)),
                null,
                null);
        buy.setItem(item, false);
        return buy;
    }

}
