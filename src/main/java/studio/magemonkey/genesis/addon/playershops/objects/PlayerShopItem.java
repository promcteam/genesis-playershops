package studio.magemonkey.genesis.addon.playershops.objects;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.GenesisShop;
import studio.magemonkey.genesis.core.conditions.GenesisCondition;
import studio.magemonkey.genesis.core.conditions.GenesisSingleCondition;
import studio.magemonkey.genesis.core.prices.GenesisPriceType;
import studio.magemonkey.genesis.inbuiltaddons.advancedshops.ActionSet;
import studio.magemonkey.genesis.inbuiltaddons.advancedshops.GenesisBuyAdvanced;

public class PlayerShopItem {
    private final ItemStack item;
    @Getter
    private final double    pricePerUnit;
    @Getter
    private int   amount;
    @Getter
    private int[] levels;


    public PlayerShopItem(ItemStack item, int amount, double pricePerUnit) {
        this.item = item;
        this.item.setAmount(1);
        this.amount = amount;
        this.pricePerUnit = pricePerUnit;
        loadLevels();
    }


    public PlayerShopItem(ConfigurationSection section) {
        this.item = section.getItemStack("a");
        this.item.setAmount(1);
        this.amount = section.getInt("b");
        this.pricePerUnit = section.getDouble("c");
        loadLevels();
    }

    public void loadLevels() {
        levels = null;
        switch (item.getMaxStackSize()) {
            case 1:
                levels = new int[]{1};
                break;
            case 8:
                levels = new int[]{1, 8};
                break;
            case 16:
                levels = new int[]{1, 4, 16};
                break;
            case 32:
                levels = new int[]{1, 8, 32};
                break;
            case 64:
                levels = new int[]{1, 8, 64};
                break;
        }
    }

    public void store(ConfigurationSection section) {
        section.set("a", item);
        section.set("b", amount);
        section.set("c", pricePerUnit);
    }


    public ItemStack create(int amount) {
        ItemStack item = this.item.clone();
        item.setAmount(amount);
        return item;
    }

    public boolean isSimilar(ItemStack i) {
        return item.isSimilar(i);
    }

    public void increase(int amount) {
        this.amount += amount;
    }

    public void decrease(int amount, PlayerShops plugin, PlayerShop shop) {
        this.amount -= amount;
        if (getAmount() < 1) {
            plugin.getShopsManager().updateShopListing();
        }
    }

    public double getWorth() {
        return pricePerUnit * amount;
    }

    public ItemStack getItemStack() {
        return item;
    }

    public GenesisBuy createShopItemNormal(PlayerShops plugin, String name, GenesisShop shop) {
        //Actions
        Map<ClickType, ActionSet> actions = null;
        if (levels.length >= 2) {
            actions = new HashMap<>();
            actions.put(ClickType.RIGHT, new ActionSet(plugin.getGenesisListener().getRewardTypeShopItem(), plugin.getGenesisListener().getPriceTypePlayerShopsCurrency(), create(levels[1]), levels[1] * pricePerUnit, plugin.getMessages().get("ItemPreview.MessageRight"), null, null, null));
        }
        if (levels.length >= 3) {
            actions.put(ClickType.MIDDLE, new ActionSet(plugin.getGenesisListener().getRewardTypeShopItem(), plugin.getGenesisListener().getPriceTypePlayerShopsCurrency(), create(levels[2]), levels[2] * pricePerUnit, plugin.getMessages().get("ItemPreview.MessageMiddle"), null, null, null));
        }

        //Conditions
        GenesisCondition condition = new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopItemCondition(), "instock", "true");

        //Set up shopitem
        GenesisBuy buy = new GenesisBuyAdvanced(plugin.getGenesisListener().getRewardTypeShopItem(), plugin.getGenesisListener().getPriceTypePlayerShopsCurrency(), create(1),
                pricePerUnit, plugin.getMessages().get("ItemPreview.MessageLeft"), -1, null, name, condition, null, null, actions);

        ItemStack item = this.item.clone();
        ItemMeta meta = item.getItemMeta();


        boolean has_lore = meta.hasLore();
        if (!has_lore) {
            meta.setLore(new ArrayList<>());
        }

        List<String> list = meta.getLore();
        if (has_lore) { //Separate new info lore and item lore
            list.add(0, " ");
            list.add(0, " ");
        }
        if (levels.length == 1) {
            list.add(0, plugin.getMessages().get("ItemPreview.LoreAny"));
        } else {
            if (levels.length >= 3) {
                list.add(0, plugin.getMessages().get("ItemPreview.LoreMiddle"));
            }
            if (levels.length >= 2) {
                list.add(0, plugin.getMessages().get("ItemPreview.LoreRight"));
            }

            list.add(0, plugin.getMessages().get("ItemPreview.LoreLeft"));
        }

        list.add(0, " ");
        list.add(0, plugin.getMessages().get("ItemPreview.Amount"));
        list.add(0, plugin.getMessages().get("ItemPreview.Price"));

        meta.setLore(list);
        item.setItemMeta(meta);

        buy.setItem(item, false);
        buy.setShop(shop);
        return buy;
    }


    public GenesisBuy createShopItemEdit(PlayerShops plugin, String name, GenesisShop shop) {
        GenesisBuy buy = new GenesisBuy(plugin.getGenesisListener().getRewardTypeShopItemEdit(), GenesisPriceType.Nothing, create(1), null, null, -1, null, name, null, null, null);

        ItemStack item = this.item.clone();
        ItemMeta meta = item.getItemMeta();

        boolean has_lore = meta.hasLore();
        if (!has_lore) {
            meta.setLore(new ArrayList<String>());
        }

        List<String> list = meta.getLore();
        if (has_lore) { //Separate new info lore and item lore
            list.add(0, " ");
            list.add(0, " ");
        }

        list.add(0, plugin.getMessages().get("ItemEditPreview.Rest"));
        list.add(0, " ");
        list.add(0, plugin.getMessages().get("ItemEditPreview.Amount"));
        list.add(0, plugin.getMessages().get("ItemEditPreview.Price"));

        meta.setLore(list);
        item.setItemMeta(meta);
        buy.setItem(item, false);
        buy.setShop(shop);
        return buy;
    }
}
