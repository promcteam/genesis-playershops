package studio.magemonkey.genesis.addon.playershops.types;


import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.prices.GenesisPriceTypeNumber;
import studio.magemonkey.genesis.managers.ClassManager;
import studio.magemonkey.genesis.managers.misc.InputReader;
import studio.magemonkey.genesis.misc.CurrencyTools;
import studio.magemonkey.genesis.misc.CurrencyTools.GenesisCurrency;

@RequiredArgsConstructor
public class GenesisPriceTypePlayerShopCurrency extends GenesisPriceTypeNumber {
    private final PlayerShops plugin;

    public Object createObject(Object o, boolean force_final_state) {
        return InputReader.getDouble(o, -1);
    }

    public boolean validityCheck(String item_name, Object o) {
        if ((Double) o != -1) {
            return true;
        }
        ClassManager.manager.getBugFinder()
                .severe("Was not able to create ShopItem " + item_name + "! PlayerShops did something wrong.");
        return false;
    }

    public void enableType() {

        //Money
        if (plugin.getSettings().getPriceType() == Money) {
            ClassManager.manager.getSettings().setMoneyEnabled(true);
            ClassManager.manager.getSettings().setVaultEnabled(true);

            //Points
        } else if (plugin.getSettings().getPriceType() == Points) {
            ClassManager.manager.getSettings().setPointsEnabled(true);
        }
    }

    @Override
    public boolean hasPrice(Player p,
                            GenesisBuy buy,
                            Object price,
                            ClickType clickType,
                            int multiplier,
                            boolean messageOnFailure) {
        double cost = (Double) price * multiplier;
        return CurrencyTools.hasValue(p,
                GenesisCurrency.detectCurrency(plugin.getSettings().getPriceType().name()),
                cost,
                messageOnFailure);
    }

    @Override
    public String takePrice(Player p, GenesisBuy buy, Object price, ClickType clickType, int multiplier) {
        double cost = (Double) price * multiplier;
        return CurrencyTools.takePrice(p,
                GenesisCurrency.detectCurrency(plugin.getSettings().getPriceType().name()),
                cost);
    }

    @Override
    public String getDisplayBalance(Player p, GenesisBuy buy, Object price, ClickType clickType) {
        return null;
    }

    @Override
    public String getDisplayPrice(Player p, GenesisBuy buy, Object price, ClickType clickType) {
        return CurrencyTools.getDisplayPrice(GenesisCurrency.detectCurrency(plugin.getSettings().getPriceType().name()),
                (Double) price);
    }

    @Override
    public String[] createNames() {
        return new String[]{"playerpointscurrency"};
    }

    public boolean supportsMultipliers() {
        return false;
    }

    @Override
    public boolean mightNeedShopUpdate() {
        return true;
    }

    @Override
    public boolean isIntegerValue() {
        return false;
    }
}
