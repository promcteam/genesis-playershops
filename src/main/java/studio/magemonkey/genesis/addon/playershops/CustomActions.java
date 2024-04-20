package studio.magemonkey.genesis.addon.playershops;

import org.bukkit.entity.Player;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShop;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShopsUserInputRename;
import studio.magemonkey.genesis.core.GenesisCustomActions;
import studio.magemonkey.genesis.core.GenesisShopHolder;
import studio.magemonkey.genesis.managers.ClassManager;
import studio.magemonkey.genesis.misc.CurrencyTools;
import studio.magemonkey.genesis.misc.CurrencyTools.GenesisCurrency;

public class CustomActions implements GenesisCustomActions {
    public final static int ACTION_CREATE_SHOP   = 0;
    public final static int ACTION_OPEN_SHOP     = 1;
    public final static int ACTION_EDIT_SHOP     = 2;
    public final static int ACTION_SAVE_SHOP     = 3;
    public final static int ACTION_SHOP_INFO     = 4;
    public final static int ACTION_RENT_FIRST    = 5;
    public final static int ACTION_RENT_INCREASE = 6;
    public final static int ACTION_SLOT_BUY      = 7;
    public final static int ACTION_SELECT_ICON   = 8;
    public final static int ACTION_RENAME_SHOP   = 9;


    private final PlayerShops plugin;

    public CustomActions(PlayerShops plugin) {
        this.plugin = plugin;
    }

    @Override
    public void customAction(Player p, int id) {
        PlayerShop shop;


        switch (id) {//TODO: info messages
            case ACTION_CREATE_SHOP:
                shop = new PlayerShop(plugin, p);
                plugin.getShopsManager().addPlayerShop(shop);
                shop.createShop();
                plugin.getShopsManager().updateShopListing();
                break;

            case ACTION_OPEN_SHOP:
                shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
                if (shop != null) {
                    if (shop.getShop() != null) {
                        shop.getShop().openInventory(p);
                    } else if (shop.getShopEdit() != null) {
                        shop.getShopEdit().openInventory(p);
                    }
                }
                break;

            case ACTION_EDIT_SHOP:
                shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
                if (shop != null) {
                    shop.tryEdit(p, true);
                }
                break;

            case ACTION_SAVE_SHOP:
                shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
                if (shop != null) {
                    boolean shopOpened = false;
                    if (ClassManager.manager.getPlugin().getAPI().isValidShop(p.getOpenInventory())) {
                        GenesisShopHolder holder =
                                ((GenesisShopHolder) p.getOpenInventory().getTopInventory().getHolder());
                        if (holder.getShop().equals(shop.getShopEdit())) {
                            shopOpened = true;
                        }
                    }
                    shop.finishEdit(true);
                    if (shopOpened) { //if shop was opened re-open new shop
                        shop.getShop().openInventory(p);
                    }
                }
                break;

            case ACTION_RENT_FIRST:
                shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
                if (shop != null) {
                    if (plugin.getSettings().getRentPlayerLimit() != -1
                            && plugin.getSettings().getRentPlayerLimit() <= plugin.getShopsManager()
                            .getRentingPlayersAmount()) {
                        plugin.getMessages().sendMessage("Message.RentingLimitReached", p, p);
                        break;
                    }
                    shop.payRent();
                    plugin.getMessages().sendMessage("Message.IncreasedRent", p, p);
                }
                break;

            case ACTION_RENT_INCREASE:
                shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
                if (shop != null) {
                    shop.payRent();
                    plugin.getMessages().sendMessage("Message.IncreasedRent", p, p);
                }
                break;

            case ACTION_SHOP_INFO:
                shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
                if (shop != null) {
                    shop.giveReward(p);
                }
                break;

            case ACTION_RENAME_SHOP:
                shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
                if (shop != null) {
                    String text = plugin.getMessages().get("Message.EnterShopName");
                    new PlayerShopsUserInputRename(shop, p.getUniqueId()).getUserInput(p, text, null, text);
                }
                break;

            case ACTION_SLOT_BUY:
                shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
                GenesisCurrency currency = GenesisCurrency.detectCurrency(plugin.getSettings().getPriceType().name());
                double price = plugin.getSettings().getSlotsPriceReal(p, plugin);
                if (shop != null) {
                    if (CurrencyTools.hasValue(p, currency, price, true)) {
                        CurrencyTools.takePrice(p, currency, price);
                        shop.increaseSlots(p);
                        plugin.getMessages().sendMessage("Message.IncreasedSlots", p, p);
                    }
                }
                break;

            case ACTION_SELECT_ICON:
                if (plugin.getIconManager().getIconSelectionShop() != null) {
                    plugin.getIconManager().getIconSelectionShop().openInventory(p);
                }
                break;

        }

    }

}
