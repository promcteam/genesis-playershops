package studio.magemonkey.genesis.addon.playershops.objects;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.addon.playershops.managers.SaveManager.REASON_SAVE;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.GenesisShop;
import studio.magemonkey.genesis.core.prices.GenesisPriceType;
import studio.magemonkey.genesis.core.rewards.GenesisRewardType;
import studio.magemonkey.genesis.managers.ClassManager;
import studio.magemonkey.genesis.misc.CurrencyTools;
import studio.magemonkey.genesis.misc.CurrencyTools.GenesisCurrency;
import studio.magemonkey.genesis.misc.Misc;

import java.io.File;
import java.util.*;

public class PlayerShop extends PlayerShopSimple {
    @Getter
    @Setter
    private GenesisShop shop, shopEdit;
    @Getter
    private boolean            beingEdited;
    private long               last_edit;
    private Map<UUID, Double>  moneySpent;
    private Map<UUID, Integer> itemsBought;
    @Setter
    @Getter
    private int                rank = -1;

    public PlayerShop(PlayerShops plugin, Player owner) {
        super(plugin, owner);
    }

    public PlayerShop(PlayerShops plugin, UUID owner) {
        super(plugin, owner);
    }

    @Override
    public void delete(File backupFolder) {
        super.delete(backupFolder);
        if (shop != null) {
            ClassManager.manager.getShops().unloadShop(shop);
            shop = null;
        }
        if (shopEdit != null) {
            ClassManager.manager.getShops().unloadShop(shopEdit);
        }
        if (shopEdit != null) {
            ClassManager.manager.getShops().unloadShop(shopEdit);
            shopEdit = null;
        }
    }

    @Override
    public void ownerJoin(Player p) {
        super.ownerJoin(p);
        moneySpent = new HashMap<>();
        itemsBought = new HashMap<>();
    }

    @Override
    public void ownerLeave(Player p) {
        super.ownerLeave(p);
        if (isBeingEdited()) {
            getPlugin().getSaveManager().saveShop(this, p, REASON_SAVE.EDITMODE_OWNER_QUIT);
        } else {
            getPlugin().getSaveManager().saveShop(this, p, REASON_SAVE.OWNER_QUIT);
        }
        moneySpent = null;
        itemsBought = null;
    }

    public void playerLeave(Player visitor) {
        updateWorth();
        double money  = getMoneySpentSoFar(visitor);
        int    amount = getItemsBoughtSoFar(visitor);
        if (money != 0) {
            moneySpent.remove(visitor.getUniqueId());
            Player owner = Bukkit.getPlayer(getOwner());
            if (owner != null) {
                String reward = CurrencyTools.getDisplayPrice(GenesisCurrency.detectCurrency(getPlugin().getSettings()
                        .getPriceType()
                        .name()), money);
                ClassManager.manager.getMessageHandler()
                        .sendMessageDirect(ClassManager.manager.getStringManager()
                                .transform(getPlugin().getMessages()
                                        .get("Message.PlayerPurchasedFromYou")
                                        .replace("%other%", visitor.getDisplayName()), owner)
                                .replace("%reward%", reward)
                                .replace("%amount%", String.valueOf(amount)), owner);
                Misc.playSound(owner, getPlugin().getSettings().getSoundPlayerPurchasedFromYou());
            }
        }
    }


    public GenesisShop getCurrentShop() {
        return shop == null ? shopEdit : shop;
    }

    public boolean canEdit(Player p, boolean fail_message) {
        if (shop != null) {
            if (shop.isBeingAccessed(p)) {
                if (fail_message) {
                    getPlugin().getMessages().sendMessage("Message.ShopBeingUsed", p, null, p, shop, null, null);
                }
                return false;
            }
            if (getEditDelayRemaining() > 0) {
                if (fail_message) {
                    getPlugin().getMessages().sendMessage("Message.ShopEditDelay", p, null, p, shop, null, null);
                }
                return false;
            }
        }
        return true;
    }

    public long getEditDelayRemaining() {
        return last_edit + getPlugin().getSettings().getEditDelay() * 1000L - System.currentTimeMillis();
    }

    public double getMoneySpentSoFar(Player p) {
        if (moneySpent != null) {
            if (moneySpent.containsKey(p.getUniqueId())) {
                return moneySpent.get(p.getUniqueId());
            }
        }
        return 0;
    }

    public int getItemsBoughtSoFar(Player p) {
        if (itemsBought != null) {
            if (itemsBought.containsKey(p.getUniqueId())) {
                return itemsBought.get(p.getUniqueId());
            }
        }
        return 0;
    }


    public GenesisShop createShop() {
        if (shop == null) {
            shop = getPlugin().getShopCreator().createShop(this, true);
        }
        return shop;
    }

    public GenesisShop createShopEdit(Player p) {
        if (shopEdit == null) {
            shopEdit = getPlugin().getShopCreator().createShopEdit(this, true);
        }
        return shopEdit;
    }

    public void unload() {
        if (isBeingEdited()) {
            finishEdit(false);
        }
        if (shopEdit != null) {
            ClassManager.manager.getShops().unloadShop(shopEdit);
            shopEdit = null;
        }
        if (shop != null) {
            ClassManager.manager.getShops().unloadShop(shop);
            shop = null;
        }
        getPlugin().getShopsManager().removePlayerShop(getOwner());
    }


    public GenesisBuy createShopLink() {
        GenesisBuy buy = new GenesisBuy(GenesisRewardType.Shop,
                GenesisPriceType.Nothing,
                getShopName(),
                null,
                null,
                -1,
                null,
                getShopName());
        buy.setItem(getIcon(), true);
        return buy;
    }


    public void close() {
        if (shop != null) {
            shop.close();
        }
        if (shopEdit != null) {
            shopEdit.close();
        }
    }


    @Override
    public void addItem(PlayerShopItem item) {
        super.addItem(item);
        if (isBeingEdited()) {
            GenesisBuy shopItem = item.createShopItemEdit(getPlugin(), createNextItemName(), shopEdit);
            shopEdit.addShopItem(shopItem, shopItem.getItem(), ClassManager.manager);
            shopEdit.finishedAddingItems();
        }
    }

    @Override
    public void removeItem(PlayerShopItem item) {
        if (isBeingEdited()) {
            GenesisBuy toRemove = null;
            for (GenesisBuy buy : shopEdit.getItems()) {
                ItemStack i = (ItemStack) buy.getReward(null);
                if (i != null) {
                    PlayerShopItem ps = getShopItem(i);
                    if (ps == item) {
                        toRemove = buy;
                        break;
                    }
                }
            }
            if (toRemove != null) {
                shopEdit.removeShopItem(toRemove);
            }
            shopEdit.finishedAddingItems();
        } else {
            ClassManager.manager.getBugFinder()
                    .warn("[PlayerShops] Shopitem removed although shop not being in edit mode.");
        }
        super.removeItem(item);
    }


    public boolean tryEdit(Player p, boolean fail_message) {
        if (canEdit(p, fail_message)) {
            startEdit(p);
            return true;
        }
        return false;
    }

    public void startEdit(Player p) {
        if (shop != null) {
            ClassManager.manager.getShops().unloadShop(shop);
            shop = null;
        }
        getPlugin().getShopsManager().updateShopListing();
        createShopEdit(p);
        beingEdited = true;

        if (getPlugin().getSettings().getRemoveItemsOutOfStock()) {
            List<PlayerShopItem> to_remove = null;
            for (GenesisBuy buy : shopEdit.getItems()) {
                ItemStack i = (ItemStack) buy.getReward(null);
                if (i != null) {
                    PlayerShopItem ps = getShopItem(i);
                    if (ps.getAmount() == 0) {
                        if (to_remove == null) {
                            to_remove = new ArrayList<PlayerShopItem>();
                        }
                        to_remove.add(ps);
                    }
                }
            }
            if (to_remove != null) {
                for (PlayerShopItem ps : to_remove) {
                    removeItem(ps);
                }
            }
        }

        shopEdit.openInventory(p);
    }

    public boolean finishEdit(boolean create_real_shop) {
        if (isBeingEdited()) {
            if (shopEdit != null) {
                ClassManager.manager.getShops().unloadShop(shopEdit);
                shopEdit = null;
            }
            save();
            if (create_real_shop) {
                createShop();
                getPlugin().getShopsManager().updateShopListing();
            }
            beingEdited = false;
            last_edit = System.currentTimeMillis();
            return true;
        }
        return false;
    }


    @Override
    public void increaseRewardIncludingTax(double d, Player reason, int amount_bought) {
        super.increaseRewardIncludingTax(d, reason, amount_bought);
        Player owner = Bukkit.getPlayer(getOwner());
        if (owner != null) {
            if (moneySpent != null) {
                double spent_so_far = getMoneySpentSoFar(reason);
                moneySpent.put(reason.getUniqueId(), spent_so_far + d);
            }
            if (itemsBought != null) {
                int bought_so_far = getItemsBoughtSoFar(reason);
                itemsBought.put(reason.getUniqueId(), bought_so_far + amount_bought);
            }
        }
    }


    private String createNextItemName() {
        if (isBeingEdited()) {
            return createNextItemName(shopEdit);
        } else {
            return createNextItemName(shop);
        }
    }

    private String createNextItemName(GenesisShop shop) {
        int    i    = 0;
        String name = String.valueOf(i);
        while (shop.getItem(name) != null) {
            i++;
            name = String.valueOf(i);
        }

        return name;
    }

}
