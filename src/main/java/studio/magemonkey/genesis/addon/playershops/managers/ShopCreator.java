package studio.magemonkey.genesis.addon.playershops.managers;

import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShop;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShopItem;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.GenesisShop;
import studio.magemonkey.genesis.core.GenesisShops;
import studio.magemonkey.genesis.managers.ClassManager;

public class ShopCreator {
    public GenesisShop createShopIconSelector(PlayerShops plugin, ShopIconManager im, boolean register) {
        GenesisShop shop = new GenesisShop(ClassManager.manager.getShops().createId(),
                "playershops_icons",
                null,
                true,
                plugin.getGenesis(),
                plugin.getMessages().get("InventoryName.Icons"),
                0,
                null) {
            @Override
            public void reloadShop() {
            }
        };
        shop.setCustomizable(true);

        if (register) {
            ClassManager.manager.getShops().addShop(shop);
        }
        return shop;
    }

    public GenesisShop createShopList(PlayerShops plugin, GenesisShops shophandler) {
        GenesisShop shopList = new GenesisShop(shophandler.createId(),
                "playershops_list",
                null,
                true,
                plugin.getGenesis(),
                plugin.getMessages().get("InventoryName.ShopList"),
                0,
                null) {
            @Override
            public void reloadShop() {
            }
        };
        shopList.setCustomizable(true);
        shophandler.addShop(shopList);
        return shopList;
    }

    public GenesisShop createShopEdit(PlayerShop s, boolean register) {
        GenesisShop shopedit = new GenesisShop(ClassManager.manager.getShops().createId(),
                s.getShopEditName(),
                null,
                true,
                ClassManager.manager.getPlugin(),
                s.getPlugin().getMessages().get("InventoryName.EditShop").replace("%owner%", s.getOwnerName()),
                0,
                null) {
            @Override
            public void reloadShop() {
            }
        };

        int i = 0;
        for (PlayerShopItem item : s.getItems()) {
            GenesisBuy shopitem = item.createShopItemEdit(s.getPlugin(), String.valueOf(i), shopedit);
            shopedit.addShopItem(shopitem, shopitem.getItem(), ClassManager.manager);
            i++;
        }
        shopedit.setCustomizable(true);
        s.setShopEdit(shopedit);
        shopedit.finishedAddingItems();

        if (register) {
            ClassManager.manager.getShops().addShop(shopedit);
        }

        return shopedit;
    }


    public GenesisShop createShop(PlayerShop s, boolean register) {
        GenesisShop shop = new GenesisShop(ClassManager.manager.getShops().createId(),
                s.getShopName(),
                null,
                true,
                ClassManager.manager.getPlugin(),
                s.getPlugin().getMessages().get("InventoryName.PlayerShop").replace("%owner%", s.getOwnerName()),
                0,
                null) {
            @Override
            public void reloadShop() {
            }
        };

        int i = 0;
        for (PlayerShopItem item : s.getItems()) {
            GenesisBuy shopitem = item.createShopItemNormal(s.getPlugin(), String.valueOf(i), shop);
            shop.addShopItem(shopitem, shopitem.getItem(), ClassManager.manager);
            i++;
        }
        shop.setCustomizable(true);
        s.setShop(shop);
        shop.finishedAddingItems();

        if (register) {
            ClassManager.manager.getShops().addShop(shop);
        }

        return shop;
    }
}
