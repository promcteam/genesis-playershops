package studio.magemonkey.genesis.addon.playershops;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShop;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShopSimple;
import studio.magemonkey.genesis.core.prices.GenesisPriceType;
import studio.magemonkey.genesis.managers.ClassManager;
import studio.magemonkey.genesis.managers.misc.InputReader;
import studio.magemonkey.genesis.misc.CurrencyTools;
import studio.magemonkey.genesis.misc.MathTools;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Settings {

    private       GenesisPriceType priceType;
    private final String           priceTypeEntry;
    private final boolean          permissions;

    private final double creationPrice;
    private final int    creationSlots;

    private final boolean              slotsEnabled;
    private final String               slotsPrice;
    @Getter
    private final int                  slotsAmount;
    @Getter
    private final int                  slotsLimit;
    private final Map<String, Integer> slotPermissions;
    private final boolean              slotPermissionsEnabled;
    @Getter
    private final int                  editDelay;

    private final List<String> ranking;

    private final boolean rentEnabled, rentSortAfterAmount, rentAllowStacking;
    @Getter
    private final double rentPrice, rentPeriodDecrease;
    @Getter
    private final long rentPeriod;
    @Getter
    private final int  rentPlayerLimit;

    private final boolean listOnlinePlayersOnly;
    @Getter
    private final double  tax;
    @Getter
    private final double  priceMin, priceMax;

    @Getter
    private final String soundPlayerPurchasedFromYou;
    private final String signsShopText, signsListingText;
    private final boolean signsEnabled;

    private final boolean preventSellingPluginItems;
    private final boolean preventCreativeAccess;
    private final boolean removeItemsOutOfStock;

    public Settings(FileConfiguration config) {
        this.priceTypeEntry = config.getString("PriceType");
        this.creationPrice = InputReader.getDouble(config.get("ShopCreation.Price"), 1000);
        this.creationSlots = InputReader.getInt(config.get("ShopCreation.Slots"), 18);
        this.permissions = config.getBoolean("EnablePermissions");

        this.slotsEnabled = config.getBoolean("SlotPurchase.Enabled");
        this.slotsPrice = config.getString("SlotPurchase.Price");
        this.slotsAmount = InputReader.getInt(config.get("SlotPurchase.Amount"), 3);
        this.slotsLimit = InputReader.getInt(config.get("SlotPurchase.TotalLimit"), 54);
        this.slotPermissionsEnabled = config.getBoolean("SlotPermissions.Enabled");
        this.slotPermissions = new HashMap<>();
        for (String line : config.getStringList("SlotPermissions.List")) {
            String[] parts = line.split(":");
            slotPermissions.put(parts[0], InputReader.getInt(parts[1], 0));
        }
        this.editDelay = InputReader.getInt(config.get("ShopEditDelay"), 60);

        this.ranking = config.getStringList("ShopRanking.List");
        this.listOnlinePlayersOnly = config.getBoolean("ShopRanking.ListOnlinePlayerShopsOnly");


        this.rentEnabled = config.getBoolean("Renting.Enabled");
        this.rentSortAfterAmount = config.getBoolean("Renting.SortAfterRentAmount");
        this.rentAllowStacking = config.getBoolean("Renting.AllowStacking");
        this.rentPrice = InputReader.getInt(config.get("Renting.Price"), 750);
        this.rentPeriodDecrease = InputReader.getDouble(config.get("Renting.PeriodDecrease"), 750);
        this.rentPeriod = InputReader.getInt(config.get("Renting.Period"), 60 * 60 * 24 * 30);
        this.rentPlayerLimit = InputReader.getInt(config.get("Renting.PlayerLimit"), 18);

        this.tax = InputReader.getDouble(config.get("Tax"), 0);
        this.priceMin = InputReader.getDouble(config.get("Price.Minimum"), 0);
        this.priceMax = InputReader.getDouble(config.get("Price.Maximum"), 75000);
        this.soundPlayerPurchasedFromYou = config.getString("Sound.PlayerPurchasedFromYou");

        this.signsShopText = config.getString("Signs.PlayerShopText");
        this.signsListingText = config.getString("Signs.ShopListingText");
        this.signsEnabled = config.getBoolean("Signs.Enabled");

        this.preventSellingPluginItems = config.getBoolean("PreventSellingPluginItems");
        this.preventCreativeAccess = config.getBoolean("PreventCreativeAccess");
        this.removeItemsOutOfStock = config.getBoolean("RemoveItemsOutOfStock");
    }

    private void updatePriceType() {
        if (priceType == null) {
            priceType = GenesisPriceType.detectType(priceTypeEntry);
            if (CurrencyTools.GenesisCurrency.detectCurrency(priceType.name()) == null) {
                ClassManager.manager.getBugFinder()
                        .severe("[PlayerShops] Unable to work with given PriceType. Automatically picking Exp in order to make the PlayerShops addon work. If you want something else please configure one of following supported PriceTypes: 'money', 'points' or 'exp'.");
                priceType = GenesisPriceType.Exp;
            }
        }
    }

    public GenesisPriceType getPriceType() {
        updatePriceType();
        return priceType;
    }

    public boolean getPermissionsEnabled() {
        return permissions;
    }

    public String getPermission(String node) {
        return permissions ? node : null;
    }

    public double getShopCreationPrice() {
        return creationPrice;
    }

    public int getShopCreationSlots() {
        return creationSlots;
    }

    public boolean getSlotsEnabled() {
        return slotsEnabled;
    }

    public double getSlotsPriceReal(Player p, PlayerShops plugin) {
        PlayerShop playerShop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
        if (playerShop == null) {
            return 0;
        }
        String price =
                ClassManager.manager.getStringManager().transform(slotsPrice, null, playerShop.getShopEdit(), null, p);
        double d = MathTools.calculate(price, 0);
        return ClassManager.manager.getMultiplierHandler().calculatePriceWithMultiplier(p, priceType, d);
    }

    public int getAdditionalSlots(Player p) {
        int add = 0;
        if (slotPermissionsEnabled) {
            for (String permission : slotPermissions.keySet()) {
                if (p.hasPermission(permission)) {
                    add += slotPermissions.get(permission);
                }
            }
        }
        return add;
    }

    public boolean getRentEnabled() {
        return rentEnabled;
    }

    public boolean getRentSortAfterAmount() {
        return rentSortAfterAmount;
    }

    public boolean getRentAllowStacking() {
        return rentAllowStacking;
    }

    public boolean getListOnlinePlayersOnly() {
        return listOnlinePlayersOnly;
    }

    public String getSignsTextPlayerShop() {
        return signsShopText;
    }

    public String getSignsTextShopListing() {
        return signsListingText;
    }

    public boolean getSignsEnabled() {
        return signsEnabled;
    }

    public boolean getPreventSellingPluginsItems() {
        return preventSellingPluginItems;
    }

    public boolean getPreventCreativeAccess() {
        return preventCreativeAccess;
    }

    public boolean getRemoveItemsOutOfStock() {
        return removeItemsOutOfStock;
    }

    public int getShopPriority(PlayerShopSimple shop, Player p) {
        if (ranking == null) {
            return 0;
        }
        for (int i = 0; i < ranking.size(); i++) {
            String s = ranking.get(i);

            if (s.equalsIgnoreCase("renting")) {
                if (shop.getRentTimeLeft(false, true) > 0) {
                    return ranking.size() - i;
                }
            }
            if (p.hasPermission(s)) {
                return ranking.size() - i;
            }
        }
        return 0;
    }

    public int getRentingPriority() {
        for (int i = 0; i < ranking.size(); i++) {
            String s = ranking.get(i);

            if (s.equalsIgnoreCase("renting")) {
                return ranking.size() - i;
            }

        }
        return 0;
    }
}
