package studio.magemonkey.genesis.addon.playershops.types;

import org.bukkit.entity.Player;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShop;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.GenesisShopHolder;
import studio.magemonkey.genesis.core.conditions.GenesisConditionType;
import studio.magemonkey.genesis.managers.misc.InputReader;

public class GenesisConditionTypePlayerShop extends GenesisConditionType {

    private final PlayerShops plugin;

    public GenesisConditionTypePlayerShop(PlayerShops plugin) {
        this.plugin = plugin;
    }


    @Override
    public boolean dependsOnPlayer() {
        return true;
    }

    @Override
    public String[] createNames() {
        return new String[]{"playershop"};
    }


    @Override
    public void enableType() {
    }


    @Override
    public boolean meetsCondition(GenesisShopHolder holder,
                                  GenesisBuy shopIte,
                                  Player p,
                                  String conditionType,
                                  String condition) {
        if (conditionType.equalsIgnoreCase("own")) {
            PlayerShop shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
            if (shop == null) {
                return !InputReader.getBoolean(condition, true);
            }
            return InputReader.getBoolean(condition, true) == (shop.getShop() == holder.getShop());
        }
        if (conditionType.equalsIgnoreCase("ownany")) {
            return InputReader.getBoolean(condition, true) == (plugin.getShopsManager().getPlayerShop(p.getUniqueId())
                    != null);
        }

        if (conditionType.equalsIgnoreCase("renting")) {
            PlayerShop shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
            if (shop == null) {
                return !InputReader.getBoolean(condition, true);
            }
            if (shop.getShop() != holder.getShop()) {
                return !InputReader.getBoolean(condition, true);
            }
            return InputReader.getBoolean(condition, true) == shop.getRentTimeLeft(false, true) > 0;
        }
        if (conditionType.equalsIgnoreCase("rentingany")) {
            PlayerShop shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
            if (shop == null) {
                return !InputReader.getBoolean(condition, true);
            }
            return InputReader.getBoolean(condition, true) == shop.getRentTimeLeft(false, true) > 0;
        }
        if (conditionType.equalsIgnoreCase("canrent")) {
            int limit = plugin.getSettings().getRentPlayerLimit();
            if (limit == -1) {
                return InputReader.getBoolean(condition, true);
            }
            return InputReader.getBoolean(condition, true) == limit > plugin.getShopsManager()
                    .getRentingPlayersAmount();
        }
        if (conditionType.equalsIgnoreCase("allowinventoryitem")) {
            PlayerShop shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
            if (shop == null) {
                return !InputReader.getBoolean(condition, true);
            }
            return InputReader.getBoolean(condition, true) == plugin.getIconManager().getAllowInventoryItem(p, shop);
        }
        if (conditionType.equalsIgnoreCase("allowshoprename")) {
            PlayerShop shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
            if (shop == null) {
                return !InputReader.getBoolean(condition, true);
            }
            return InputReader.getBoolean(condition, true) == plugin.getIconManager().getAllowShopRename(p, shop)
                    && shop.getCurrentShop() == holder.getShop();
        }

        return false;
    }


    @Override
    public String[] showStructure() {
        return new String[]{"own:[boolean]", "renting:[boolean]"};
    }


}
