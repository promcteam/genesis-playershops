package com.promcteam.genesis.addon.playershops.listener;


import com.promcteam.genesis.addon.playershops.PlayerShops;
import com.promcteam.genesis.addon.playershops.objects.PlayerShop;
import com.promcteam.genesis.addon.playershops.objects.PlayerShopItem;
import com.promcteam.genesis.addon.playershops.types.GenesisConditionTypePlayerShop;
import com.promcteam.genesis.addon.playershops.types.GenesisConditionTypePlayerShopItem;
import com.promcteam.genesis.addon.playershops.types.GenesisConditionTypePlayerShopSlots;
import com.promcteam.genesis.addon.playershops.types.GenesisPriceTypePlayerShopCurrency;
import com.promcteam.genesis.addon.playershops.types.GenesisRewardTypePlayerShopIcon;
import com.promcteam.genesis.addon.playershops.types.GenesisRewardTypePlayerShopItem;
import com.promcteam.genesis.addon.playershops.types.GenesisRewardTypePlayerShopItemEdit;
import org.black_ixx.bossshop.core.BSShop;
import org.black_ixx.bossshop.core.conditions.BSConditionType;
import org.black_ixx.bossshop.core.prices.BSPriceType;
import org.black_ixx.bossshop.core.rewards.BSRewardType;
import org.black_ixx.bossshop.events.BSCheckStringForFeaturesEvent;
import org.black_ixx.bossshop.events.BSChoosePageLayoutEvent;
import org.black_ixx.bossshop.events.BSRegisterTypesEvent;
import org.black_ixx.bossshop.events.BSTransformStringEvent;
import org.black_ixx.bossshop.managers.features.PageLayoutHandler;
import org.black_ixx.bossshop.misc.CurrencyTools;
import org.black_ixx.bossshop.misc.TimeTools;
import org.black_ixx.bossshop.misc.CurrencyTools.BSCurrency;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class GenesisListener implements Listener {

    private final PlayerShops plugin;
    private final BSConditionType playershop, playershopslots, playershopitem;
    private final BSRewardType rewardtypeshopitem, rewardtypeshopitemedit, rewardtypeshopicon;
    private final BSPriceType pricetypeshopcurrency;

    public GenesisListener(PlayerShops plugin) {
        this.plugin = plugin;
        this.playershop = new GenesisConditionTypePlayerShop(plugin);
        this.playershopslots = new GenesisConditionTypePlayerShopSlots(plugin);
        this.playershopitem = new GenesisConditionTypePlayerShopItem(plugin);
        this.rewardtypeshopitem = new GenesisRewardTypePlayerShopItem(plugin);
        this.rewardtypeshopitemedit = new GenesisRewardTypePlayerShopItemEdit(plugin);
        this.rewardtypeshopicon = new GenesisRewardTypePlayerShopIcon(plugin);
        this.pricetypeshopcurrency = new GenesisPriceTypePlayerShopCurrency(plugin);
    }


    @EventHandler
    public void register(BSRegisterTypesEvent event) {
        this.playershop.register();
        this.playershopslots.register();
        this.playershopitem.register();
    }


    @EventHandler
    public void chooseLayout(BSChoosePageLayoutEvent event) {
        BSShop shop = event.getShop();
        if (plugin.getShopsManager() != null) {
            PageLayoutHandler layout = plugin.getShopsManager().getLayout(shop, plugin);
            if (layout != null) {
                event.setLayout(layout);
            }
        }
    }

    @EventHandler
    public void checkString(BSCheckStringForFeaturesEvent event) {
        if (event.getText() == null) {
            return;
        }
        if (event.getText().contains("%playershop")) {
            if (event.getText().contains("%playershops_slots_current%")) {
                event.approveFeature();
            }
            if (event.getText().contains("%playershops_rank_current%")) {
                event.approveFeature();
            }
            if (event.getText().contains("%playershops_rank_max%")) {
                event.approveFeature();
            }
            if (event.getText().contains("%playershops_slots_price%")) {
                event.approveFeature();
            }

            if (event.getText().contains("%playershops_stock%")) {
                event.approveFeature();
            }

            if (event.getText().contains("%playershops_price%")) {
                event.approveFeature();
            }

            if (event.getText().contains("%playershops_money%")) {
                event.approveFeature();
            }

            if (event.getText().contains("%playershops_worth")) {
                event.approveFeature();
            }

            if (event.getText().contains("%playershops_renting_timeleft%")) {
                event.approveFeature();
            }

            if (event.getText().contains("%playershops_edittime%")) {
                event.approveFeature();
            }

            if (event.getText().contains("%playershops_amount")) {
                event.approveFeature();
            }

            if (event.getText().contains("%playershopname%")) {
                event.approveFeature();
            }
        }
    }

    @EventHandler
    public void transformString(BSTransformStringEvent event) {
        if (event.getText() == null) {
            return;
        }
        if (event.getText().contains("%playershops_slots_creation%")) {
            event.setText(event.getText().replace("%playershops_slots_creation%", String.valueOf(plugin.getSettings().getShopCreationSlots())));
        }

        if (event.getText().contains("%playershops_slots_increase%")) {
            event.setText(event.getText().replace("%playershops_slots_increase%", String.valueOf(plugin.getSettings().getSlotsAmount())));
        }

        if (event.getText().contains("%playershops_slots_max%")) {
            event.setText(event.getText().replace("%playershops_slots_max%", String.valueOf(plugin.getSettings().getSlotsLimit())));
        }

        if (event.getText().contains("%playershops_price_min%")) {
            event.setText(event.getText().replace("%playershops_price_min%", CurrencyTools.getDisplayPrice(BSCurrency.detectCurrency(plugin.getSettings().getPriceType().name()), plugin.getSettings().getPriceMin())));
        }

        if (event.getText().contains("%playershops_price_max%")) {
            event.setText(event.getText().replace("%playershops_price_max%", CurrencyTools.getDisplayPrice(BSCurrency.detectCurrency(plugin.getSettings().getPriceType().name()), plugin.getSettings().getPriceMax())));
        }

        if (event.getText().contains("%playershops_renting_time%")) {
            event.setText(event.getText().replace("%playershops_renting_time%", TimeTools.transform(Math.max(0, plugin.getSettings().getRentPeriod()))));
        }

        if (event.getText().contains("%playershops_renting_price%")) {
            event.setText(event.getText().replace("%playershops_renting_price%", CurrencyTools.getDisplayPrice(BSCurrency.detectCurrency(plugin.getSettings().getPriceType().name()), plugin.getSettings().getRentPrice())));
        }


        if (event.getShop() != null && plugin.getShopsManager() != null) {

            PlayerShop playershop = plugin.getShopsManager().getPlayerShop(event.getShop(), true);
            if (playershop != null) {

                if (event.getTarget() != null) {


                    if (event.getText().contains("%playershops_money%")) {
                        event.setText(event.getText().replace("%playershops_money%", CurrencyTools.getDisplayPrice(BSCurrency.detectCurrency(plugin.getSettings().getPriceType().name()), playershop.getReward())));
                    }
                    if (event.getText().contains("%playershops_worth%")) {
                        event.setText(event.getText().replace("%playershops_worth%", CurrencyTools.getDisplayPrice(BSCurrency.detectCurrency(plugin.getSettings().getPriceType().name()), playershop.getWorth())));
                    }

                    if (event.getText().contains("%playershops_rank_current%")) {
                        int rank = playershop.getRank();
                        event.setText(event.getText().replace("%playershops_rank_current%", (rank == -1 ? plugin.getMessages().get("Rest.ShopNotListed") : String.valueOf(rank))));
                    }

                    if (event.getText().contains("%playershops_rank_max%")) {
                        int rank = plugin.getShopsManager().getRankMax();
                        event.setText(event.getText().replace("%playershops_rank_max%", String.valueOf(rank)));
                    }

                }

                if (event.getText().contains("%playershops_edittime%")) {
                    event.setText(event.getText().replace("%playershops_edittime%", String.valueOf(TimeTools.transform(Math.max(0, playershop.getEditDelayRemaining() / 1000)))));
                }
                if (event.getText().contains("%playershopname%")) {
                    event.setText(event.getText().replace("%playershopname%", playershop.getShopDisplayName()));
                }

                if (event.getShopItem() != null) {
                    if (event.getShopItem().getReward(null) instanceof ItemStack) {
                        PlayerShopItem item = playershop.getShopItem((ItemStack) event.getShopItem().getReward(null));
                        if (item != null) {
                            if (event.getText().contains("%playershops_stock%") || event.getText().contains("%playershops_price%")) {
                                if (event.getTarget() != null) {
                                    event.setText(event.getText().replace("%playershops_stock%", String.valueOf(item.getAmount())));
                                }
                                event.setText(event.getText().replace("%playershops_price%", CurrencyTools.getDisplayPrice(BSCurrency.detectCurrency(plugin.getSettings().getPriceType().name()), item.getPricePerUnit())));
                            }

                            if (event.getText().contains("%playershops_amount")) {
                                event.setText(event.getText().replace("%playershops_amount%", String.valueOf(item.getLevels()[0])));
                                if (item.getLevels().length >= 2) {
                                    event.setText(event.getText().replace("%playershops_amount_right%", String.valueOf(item.getLevels()[1])));
                                }
                                if (item.getLevels().length >= 3) {
                                    event.setText(event.getText().replace("%playershops_amount_middle%", String.valueOf(item.getLevels()[2])));
                                }
                            }
                        }
                    }
                }


            }

        }


        if (event.getTarget() != null) { //Should be possible even with no shop owned (displaying 0 in that case)
            if (event.getText().contains("%playershops_slots_current%")) {
                int slots = 0;
                PlayerShop shop = plugin.getShopsManager().getPlayerShop(event.getTarget().getUniqueId());
                if (shop != null) {
                    slots = shop.getSlotsAmount(event.getTarget(), true);
                }
                event.setText(event.getText().replace("%playershops_slots_current%", String.valueOf(slots)));
            }

            if (event.getText().contains("%playershops_renting_timeleft%")) {
                long timeleft = 0;
                PlayerShop shop = plugin.getShopsManager().getPlayerShop(event.getTarget().getUniqueId());
                if (shop != null) {
                    timeleft = shop.getRentTimeLeft(true, true);
                }
                event.setText(event.getText().replace("%playershops_renting_timeleft%", TimeTools.transform(Math.max(0, timeleft / 1000))));
            }

            if (event.getText().contains("%playershopname%")) {
                PlayerShop shop = plugin.getShopsManager().getPlayerShop(event.getTarget().getUniqueId());
                if (shop != null) {
                    event.setText(event.getText().replace("%playershopname%", shop.getShopDisplayName()));
                }
            }

            if (event.getText().contains("%playershops_slots_price%")) {
                event.setText(event.getText().replace("%playershops_slots_price%", String.valueOf(plugin.getSettings().getSlotsPriceReal(event.getTarget(), plugin))));
            }
        }


    }


    public BSConditionType getPlayerShopCondition() {
        return playershop;
    }

    public BSConditionType getPlayerShopSlotsCondition() {
        return playershopslots;
    }

    public BSConditionType getPlayerShopItemCondition() {
        return playershopitem;
    }

    public BSRewardType getRewardTypeShopItem() {
        return rewardtypeshopitem;
    }

    public BSRewardType getRewardTypeShopItemEdit() {
        return rewardtypeshopitemedit;
    }

    public BSRewardType getRewardTypeShopIcon() {
        return rewardtypeshopicon;
    }

    public BSPriceType getPriceTypePlayerShopsCurrency() {
        return pricetypeshopcurrency;
    }

}
