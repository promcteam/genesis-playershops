package studio.magemonkey.genesis.addon.playershops.managers;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import studio.magemonkey.genesis.addon.playershops.CustomActions;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShop;
import studio.magemonkey.genesis.api.GenesisAddonConfig;
import studio.magemonkey.genesis.api.GenesisAddonStorage;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.GenesisCustomLink;
import studio.magemonkey.genesis.core.GenesisShop;
import studio.magemonkey.genesis.core.GenesisShops;
import studio.magemonkey.genesis.core.conditions.GenesisCondition;
import studio.magemonkey.genesis.core.conditions.GenesisConditionSet;
import studio.magemonkey.genesis.core.conditions.GenesisConditionType;
import studio.magemonkey.genesis.core.conditions.GenesisSingleCondition;
import studio.magemonkey.genesis.core.prices.GenesisPriceType;
import studio.magemonkey.genesis.core.rewards.GenesisRewardType;
import studio.magemonkey.genesis.managers.ClassManager;
import studio.magemonkey.genesis.managers.features.PageLayoutHandler;

import java.io.File;
import java.util.*;

public class PlayerShopsManager {
    private GenesisShop       shopList;
    private PageLayoutHandler shopListLayout, shopLayout, shopEditLayout, iconSelectionLayout;

    private Map<UUID, PlayerShop> playerShops;
    private List<String>          renting;
    private GenesisAddonStorage   storage;
    @Getter
    private int                   rankMax;


    public void init(PlayerShops plugin) {
        storage = new GenesisAddonConfig(plugin, "storage");
        renting = storage.getStringList("Renting");

        GenesisShops shopHandler = ClassManager.manager.getShops();
        setupLayouts(plugin);

        //Shopslist
        shopList = plugin.getShopCreator().createShopList(plugin, shopHandler);

        //add player shops
        playerShops = new HashMap<>(); //TODO: LOAD
        File shopFolder = new File(plugin.getGenesis().getDataFolder() + File.separator + "addons" + File.separator
                + plugin.getAddonName() + File.separator + "shops");
        if (shopFolder.exists()) {
            loadFile(plugin, shopFolder);
        }


        shopList.finishedAddingItems();
        plugin.getShopsManager().updateShopListing();
    }

    private void loadFile(PlayerShops plugin, File file) {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                loadFile(plugin, f);
            }
        } else {
            UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
            if (!plugin.getSettings().getListOnlinePlayersOnly()) {
                plugin.getSaveManager().loadShop(uuid, null, SaveManager.REASON_LOAD.SERVER_START, false, false);
            } else if (Bukkit.getPlayer(uuid) != null) {
                plugin.getSaveManager()
                        .loadShop(uuid,
                                Bukkit.getPlayer(uuid),
                                SaveManager.REASON_LOAD.SERVER_START_OWNER_ONLINE,
                                false,
                                false);
            } else if (renting.contains(uuid.toString())) {
                PlayerShop shop = plugin.getSaveManager()
                        .loadShop(uuid, null, SaveManager.REASON_LOAD.SERVER_START_RENTING, false, false);
                if (shop.getRentTimeLeft(false, true) <= 0) { //Renting time is out
                    shop.unload();
                    renting.remove(uuid.toString());
                }
            }
        }
    }

    public void save(PlayerShops plugin, SaveManager.REASON_SAVE reason) {
        if (playerShops != null) {
            renting.clear();
            for (PlayerShop shop : playerShops.values()) {
                plugin.getSaveManager().saveShop(shop, null, reason);
                if (shop.getRentTimeLeft(false, true) > 0) {
                    renting.add(shop.getOwner().toString());
                }
            }
            storage.set("Renting", renting);
            storage.save();
        }

        if (reason == SaveManager.REASON_SAVE.SERVER_RELOAD || reason == SaveManager.REASON_SAVE.SERVER_UNLOAD) {
            playerShops.clear();
        }
    }


    public void addPlayerShop(PlayerShop shop) {
        playerShops.put(shop.getOwner(), shop);
    }

    public void removePlayerShop(UUID uuid) {
        PlayerShop shop = getPlayerShop(uuid);
        if (shop != null) {
            shop.setRank(-1);
        }
        playerShops.remove(uuid);
    }

    public PlayerShop getPlayerShop(UUID owner) {
        return playerShops.get(owner);
    }

    public PlayerShop getPlayerShop(String owner_start) {
        synchronized (playerShops) {
            for (PlayerShop s : playerShops.values()) {
                if (s.getOwnerName().toLowerCase().startsWith(owner_start.toLowerCase())) {
                    return s;
                }
            }
        }
        return null;
    }

    public PlayerShop getPlayerShop(GenesisShop shop, boolean includeShopEdits) {
        if (shop != null) {
            synchronized (playerShops) {
                for (PlayerShop s : playerShops.values()) {
                    if (s.getShop() == shop) {
                        return s;
                    } else if (s.getShopEdit() == shop && includeShopEdits) {
                        return s;
                    }
                }
            }
        }
        return null;
    }

    public void openShoplist(Player p) {
        shopList.openInventory(p);
    }

    public int getRentingPlayersAmount() {
        int count = 0;
        for (PlayerShop shop : playerShops.values()) {
            if (shop.getRentTimeLeft(true, true) > 0) {
                count++;
            }
        }
        return count;
    }


    public void updateShopListing() {
        synchronized (shopList.getItems()) {
            shopList.getItems().clear();

            List<PlayerShop> to_add = new ArrayList<PlayerShop>();
            synchronized (playerShops.values()) {
                for (PlayerShop shop : playerShops.values()) {
                    if (shop.getShop() != null && shop.containsVisibleItems()) {
                        to_add.add(shop);
                    } else {
                        shop.setRank(-1);
                    }
                }
            }

            int rank = 0;
            while (!to_add.isEmpty()) {
                rank++;
                PlayerShop current = to_add.get(0);
                for (PlayerShop shop : to_add) {
                    shop.updateInfo(null);
                    if (shop.hasHigherPriority(current)) {
                        current = shop;
                    }
                }
                to_add.remove(current);
                addToShopListing(current);
                current.setRank(rank);
            }
            this.rankMax = rank;
            shopList.finishedAddingItems();
        }
    }

    @Deprecated
    public void addToShopListing(PlayerShop shop) {
        GenesisBuy buy = shop.createShopLink();
        shopList.addShopItem(buy, buy.getItem(), ClassManager.manager);
        //TODO
    }


    public PageLayoutHandler getLayout(GenesisShop shop, PlayerShops plugin) {
        if (shop == shopList) {
            return shopListLayout;
        }

        if (shop == plugin.getIconManager().getIconSelectionShop()) {
            return iconSelectionLayout;
        }

        PlayerShop playershop = getPlayerShop(shop, true);
        if (playershop != null) {
            if (playershop.isBeingEdited()) {
                return shopEditLayout;
            } else {
                return shopLayout;
            }
        }


        return null;
    }


    public void setupLayouts(PlayerShops plugin) {
        GenesisBuy arrowleft = plugin.getItems()
                .getArrowLeft()
                .createShopItem(GenesisRewardType.ShopPage,
                        GenesisPriceType.Nothing,
                        "previous",
                        null,
                        45,
                        null,
                        new GenesisSingleCondition(
                                GenesisConditionType.SHOPPAGE, "over", "1"));
        GenesisBuy arrowright = plugin.getItems()
                .getArrowRight()
                .createShopItem(GenesisRewardType.ShopPage,
                        GenesisPriceType.Nothing,
                        "next",
                        null,
                        53,
                        null,
                        new GenesisSingleCondition(GenesisConditionType.SHOPPAGE, "under", "%maxpage%"));
        GenesisBuy close = plugin.getItems()
                .getClose()
                .createShopItem(GenesisRewardType.Close, GenesisPriceType.Nothing, null, null, 50, null);

        //Shopslist
        {
            List<GenesisBuy> items = new ArrayList<GenesisBuy>();
            addItem(items, arrowleft);
            addItem(items, arrowright);
            addItem(items, close);
            addItem(items,
                    plugin.getItems()
                            .getOwnShop()
                            .createShopItem(GenesisRewardType.Custom,
                                    GenesisPriceType.Nothing,
                                    new GenesisCustomLink(
                                            CustomActions.ACTION_OPEN_SHOP, plugin.getActions()),
                                    null,
                                    48,
                                    null,
                                    new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopCondition(),
                                            "ownany",
                                            "true")));
            addItem(items,
                    plugin.getItems()
                            .getCreateShop()
                            .createShopItem(GenesisRewardType.Custom,
                                    plugin.getSettings().getPriceType(),
                                    new GenesisCustomLink(CustomActions.ACTION_CREATE_SHOP, plugin.getActions()),
                                    plugin.getSettings().getShopCreationPrice(),
                                    48,
                                    plugin.getSettings().getPermission("PlayerShops.create"),
                                    new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopCondition(),
                                            "ownany",
                                            "false")));
            shopListLayout = new PageLayoutHandler(items, 46, false);
        }

        //Shop
        {
            List<GenesisBuy> items = new ArrayList<GenesisBuy>();
            addItem(items, arrowleft);
            addItem(items, arrowright);
            addItem(items,
                    plugin.getItems()
                            .getShopInfo()
                            .createShopItem(GenesisRewardType.Custom,
                                    GenesisPriceType.Nothing,
                                    new GenesisCustomLink(CustomActions.ACTION_SHOP_INFO, plugin.getActions()),
                                    null,
                                    47,
                                    null,
                                    new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopCondition(),
                                            "own",
                                            "true")));

            if (plugin.getSettings().getRentAllowStacking()) {
                addItem(items,
                        plugin.getItems()
                                .getRentingIncrease()
                                .createShopItem(GenesisRewardType.Custom,
                                        plugin.getSettings().getPriceType(),
                                        new GenesisCustomLink(CustomActions.ACTION_RENT_INCREASE, plugin.getActions()),
                                        plugin.getSettings().getRentPrice(),
                                        48,
                                        plugin.getSettings().getPermission("PlayerShops.rent"),
                                        new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopCondition(),
                                                "renting",
                                                "true")));
            }

            List<GenesisCondition> conditions_rentlimit = new ArrayList<GenesisCondition>();
            conditions_rentlimit.add(new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopCondition(),
                    "renting",
                    "false"));
            conditions_rentlimit.add(new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopCondition(),
                    "canrent",
                    "false"));
            conditions_rentlimit.add(new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopCondition(),
                    "own",
                    "true"));
            addItem(items,
                    plugin.getItems()
                            .getRentingLimitReached()
                            .createShopItem(GenesisRewardType.Nothing,
                                    GenesisPriceType.Nothing,
                                    null,
                                    null,
                                    48,
                                    null,
                                    new GenesisConditionSet(conditions_rentlimit)));

            List<GenesisCondition> conditions_rentfirst = new ArrayList<GenesisCondition>();
            conditions_rentfirst.add(new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopCondition(),
                    "renting",
                    "false"));
            conditions_rentfirst.add(new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopCondition(),
                    "own",
                    "true"));
            addItem(items,
                    plugin.getItems()
                            .getRenting()
                            .createShopItem(GenesisRewardType.Custom,
                                    plugin.getSettings().getPriceType(),
                                    new GenesisCustomLink(CustomActions.ACTION_RENT_FIRST, plugin.getActions()),
                                    plugin.getSettings().getRentPrice(),
                                    48,
                                    plugin.getSettings().getPermission("PlayerShops.rent"),
                                    new GenesisConditionSet(conditions_rentfirst)));
            addItem(items,
                    plugin.getItems()
                            .getEditShop()
                            .createShopItem(GenesisRewardType.Custom,
                                    GenesisPriceType.Nothing,
                                    new GenesisCustomLink(CustomActions.ACTION_EDIT_SHOP, plugin.getActions()),
                                    null,
                                    49,
                                    null,
                                    new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopCondition(),
                                            "own",
                                            "true")));
            addItem(items,
                    plugin.getItems()
                            .getBack()
                            .createShopItem(GenesisRewardType.Shop,
                                    GenesisPriceType.Nothing,
                                    "playershops_list",
                                    null,
                                    50,
                                    null));
            shopLayout = new PageLayoutHandler(items, 46, false);
        }

        //Shopedit
        {
            List<GenesisBuy> items = new ArrayList<GenesisBuy>();
            addItem(items, arrowleft);
            addItem(items, arrowright);
            addItem(items,
                    plugin.getItems()
                            .getShopInfo()
                            .createShopItem(GenesisRewardType.Custom,
                                    GenesisPriceType.Nothing,
                                    new GenesisCustomLink(CustomActions.ACTION_SHOP_INFO, plugin.getActions()),
                                    null,
                                    47,
                                    null));
            if (plugin.getSettings().getSlotsEnabled()) {
                addItem(items,
                        plugin.getItems()
                                .getBuySlot()
                                .createShopItem(GenesisRewardType.Custom,
                                        GenesisPriceType.Nothing,
                                        new GenesisCustomLink(CustomActions.ACTION_SLOT_BUY, plugin.getActions()),
                                        null,
                                        48,
                                        plugin.getSettings().getPermission("PlayerShops.buyslot"),
                                        new GenesisSingleCondition(plugin.getGenesisListener()
                                                .getPlayerShopSlotsCondition(),
                                                "under",
                                                String.valueOf((
                                                        plugin.getSettings().getSlotsLimit() - plugin.getSettings()
                                                                .getSlotsAmount() + 1)))));
            }
            addItem(items,
                    plugin.getItems()
                            .getSaveShop()
                            .createShopItem(GenesisRewardType.Custom,
                                    GenesisPriceType.Nothing,
                                    new GenesisCustomLink(CustomActions.ACTION_SAVE_SHOP, plugin.getActions()),
                                    null,
                                    49,
                                    null));
            addItem(items,
                    plugin.getItems()
                            .getBack()
                            .createShopItem(GenesisRewardType.Shop,
                                    GenesisPriceType.Nothing,
                                    "playershops_list",
                                    null,
                                    50,
                                    null)); //Shops will stay in edit mode until either player saves or server restarts
            if (plugin.getIconManager().getAllowIconSelection()) {
                addItem(items,
                        plugin.getItems()
                                .getSelectIcon()
                                .createShopItem(GenesisRewardType.Custom,
                                        GenesisPriceType.Nothing,
                                        new GenesisCustomLink(CustomActions.ACTION_SELECT_ICON, plugin.getActions()),
                                        plugin.getSettings().getPermission("PlayerShops.selecticon"),
                                        51,
                                        null));
            }
            addItem(items,
                    plugin.getItems()
                            .getShopRename()
                            .createShopItem(GenesisRewardType.Custom,
                                    GenesisPriceType.Nothing,
                                    new GenesisCustomLink(CustomActions.ACTION_RENAME_SHOP, plugin.getActions()),
                                    null,
                                    plugin.getIconManager().getAllowIconSelection() ? 52 : 51,
                                    null,
                                    new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopCondition(),
                                            "allowshoprename",
                                            "true")));
            shopEditLayout = new PageLayoutHandler(items, 46, false);
        }

        //Iconselection
        {
            List<GenesisBuy> items = new ArrayList<GenesisBuy>();
            addItem(items, arrowleft);
            addItem(items, arrowright);
            addItem(items,
                    plugin.getItems()
                            .getSelectInventoryItemAllow()
                            .createShopItem(GenesisRewardType.Nothing,
                                    GenesisPriceType.Nothing,
                                    null,
                                    null,
                                    48,
                                    null,
                                    new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopCondition(),
                                            "allowinventoryitem",
                                            "true")));
            addItem(items,
                    plugin.getItems()
                            .getSelectInventoryItemDeny()
                            .createShopItem(GenesisRewardType.Nothing,
                                    GenesisPriceType.Nothing,
                                    null,
                                    null,
                                    48,
                                    null,
                                    new GenesisSingleCondition(plugin.getGenesisListener().getPlayerShopCondition(),
                                            "allowinventoryitem",
                                            "false")));
            addItem(items,
                    plugin.getItems()
                            .getBack()
                            .createShopItem(GenesisRewardType.Custom,
                                    GenesisPriceType.Nothing,
                                    new GenesisCustomLink(CustomActions.ACTION_OPEN_SHOP, plugin.getActions()),
                                    null,
                                    50,
                                    null));
            iconSelectionLayout = new PageLayoutHandler(items, 46, false);
        }
    }


    private void addItem(List<GenesisBuy> items, GenesisBuy buy) {
        if (buy != null) {
            items.add(buy);
        }
    }


}
