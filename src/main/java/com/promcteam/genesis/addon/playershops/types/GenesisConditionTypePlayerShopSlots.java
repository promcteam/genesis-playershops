package com.promcteam.genesis.addon.playershops.types;

import com.promcteam.genesis.addon.playershops.PlayerShops;
import com.promcteam.genesis.addon.playershops.objects.PlayerShop;
import org.black_ixx.bossshop.core.BSBuy;
import org.black_ixx.bossshop.core.BSShopHolder;
import org.black_ixx.bossshop.core.conditions.BSConditionTypeNumber;
import org.bukkit.entity.Player;

public class GenesisConditionTypePlayerShopSlots extends BSConditionTypeNumber {
    private final PlayerShops plugin;

    public GenesisConditionTypePlayerShopSlots(PlayerShops plugin) {
        this.plugin = plugin;
    }

    @Override
    public double getNumber(BSBuy shopitem, BSShopHolder holder, Player p) {
        PlayerShop shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
        if (shop == null) {
            return 0;
        }
        return shop.getSlotsAmount(p, true); //Player needs to be owner!
    }

    @Override
    public boolean dependsOnPlayer() {
        return true;
    }

    @Override
    public String[] createNames() {
        return new String[]{"playershopslots"};
    }


    @Override
    public void enableType() {
    }


}
