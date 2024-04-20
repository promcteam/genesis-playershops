package studio.magemonkey.genesis.addon.playershops.types;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShop;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.GenesisShopHolder;
import studio.magemonkey.genesis.core.conditions.GenesisConditionTypeNumber;

@RequiredArgsConstructor
public class GenesisConditionTypePlayerShopSlots extends GenesisConditionTypeNumber {
    private final PlayerShops plugin;

    @Override
    public double getNumber(GenesisBuy shopItem, GenesisShopHolder holder, Player p) {
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
