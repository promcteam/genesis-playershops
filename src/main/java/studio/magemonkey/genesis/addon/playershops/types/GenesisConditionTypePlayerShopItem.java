package studio.magemonkey.genesis.addon.playershops.types;

import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShop;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShopItem;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.GenesisShopHolder;
import studio.magemonkey.genesis.core.conditions.GenesisConditionType;
import studio.magemonkey.genesis.managers.ClassManager;
import studio.magemonkey.genesis.managers.misc.InputReader;

@RequiredArgsConstructor
public class GenesisConditionTypePlayerShopItem extends GenesisConditionType {
    private final PlayerShops plugin;

    @Override
    public boolean dependsOnPlayer() {
        return true;
    }

    @Override
    public String[] createNames() {
        return new String[]{"playershopitem"};
    }

    @Override
    public void enableType() {
    }

    @Override
    public boolean meetsCondition(GenesisShopHolder holder,
                                  GenesisBuy shopItem,
                                  Player p,
                                  String conditionType,
                                  String condition) {
        if (conditionType.equalsIgnoreCase("instock")) {
            PlayerShop playershop = plugin.getShopsManager().getPlayerShop(shopItem.getShop(), false);
            if (playershop == null) {
                ClassManager.manager.getBugFinder()
                        .severe("[PlayerShops] (Condition PlayerShopItem) Unable to detect PlayerShop via Shopitem that is connected to "
                                + shopItem.getShop());
                return false;
            }
            ItemStack      itemstack = (ItemStack) shopItem.getReward(null);
            PlayerShopItem item      = playershop.getShopItem((itemstack));
            if (item == null) {
                ClassManager.manager.getBugFinder()
                        .severe("[PlayerShops] (Condition PlayerShopItem) Unable to detect PlayerShopItem via Shopitem.");
                return false;
            }
            return InputReader.getBoolean(condition, true) == item.getAmount() >= itemstack.getAmount();
        }

        return false;
    }

    @Override
    public String[] showStructure() {
        return new String[]{"instock:[boolean]"};
    }
}
