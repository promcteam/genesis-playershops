package studio.magemonkey.genesis.addon.playershops.types;

import lombok.RequiredArgsConstructor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShop;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShopItem;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.rewards.GenesisRewardTypeItem;
import studio.magemonkey.genesis.managers.ClassManager;
import studio.magemonkey.genesis.managers.misc.InputReader;

@RequiredArgsConstructor
public class GenesisRewardTypePlayerShopItem extends GenesisRewardTypeItem {
    private final PlayerShops plugin;

    public Object createObject(Object o, boolean force_final_state) {
        if (force_final_state) {
            return InputReader.readItem(o, false);
        } else {
            return InputReader.readStringList(o);
        }
    }

    public boolean validityCheck(String item_name, Object o) {
        if (o != null) {
            return true;
        }
        ClassManager.manager.getBugFinder()
                .severe("Was not able to create ShopItem " + item_name + "! PlayerShops did something wrong.");
        return false;
    }

    @Override
    public boolean canBuy(Player p, GenesisBuy buy, boolean message_if_no_success, Object reward, ClickType clickType) {
        PlayerShop playershop = plugin.getShopsManager().getPlayerShop(buy.getShop(), false);
        if (playershop == null) {
            ClassManager.manager.getBugFinder()
                    .severe("[PlayerShops] (ShopItem) Unable to detect PlayerShop via Shopitem that is connected to "
                            + buy.getShop());
            return false;
        }
        PlayerShopItem item = playershop.getShopItem((ItemStack) buy.getReward(clickType));
        if (item == null) {
            ClassManager.manager.getBugFinder()
                    .severe("[PlayerShops] (ShopItem) Unable to detect PlayerShopItem via Shopitem.");
            return false;
        }

        if (p.getGameMode() == GameMode.CREATIVE && plugin.getSettings().getPreventCreativeAccess()) {
            plugin.getMessages()
                    .sendMessage("Message.PreventedCreativeBuyItem", p, null, p, playershop.getShopEdit(), null, null);
            return false;
        }

        ItemStack actual_reward = (ItemStack) buy.getReward(clickType);
        if (item.getAmount() < actual_reward.getAmount()) {
            if (item.getAmount() > 0) {
                plugin.getMessages().sendMessage("Message.NotEnoughInStock", p, null, p, buy.getShop(), null, buy);
            } else {
                plugin.getMessages().sendMessage("Message.OutOfStock", p, null, p, buy.getShop(), null, buy);
            }
            return false;
        }
        return true;
    }

    @Override
    public void giveReward(Player p, GenesisBuy buy, Object reward, ClickType clickType) {
        ItemStack i = (ItemStack) buy.getReward(clickType);

        //Decrease shop item stock
        PlayerShop playershop = plugin.getShopsManager().getPlayerShop(buy.getShop(), false);
        playershop.increaseReward((Double) buy.getPrice(clickType), p, i.getAmount());
        PlayerShopItem item = playershop.getShopItem((ItemStack) buy.getReward(clickType));
        item.decrease(i.getAmount(), plugin, playershop);

        //Give item to player
        ClassManager.manager.getItemStackCreator().giveItem(p, buy, i, true);
    }

    @Override
    public String getDisplayReward(Player p, GenesisBuy buy, Object reward, ClickType clickType) {
        ItemStack i               = (ItemStack) buy.getReward(clickType);
        String    items_formatted = ClassManager.manager.getItemStackTranslator().readItemStack(i);
        return ClassManager.manager.getMessageHandler().get("Display.Item").replace("%items%", items_formatted);
    }

    @Override
    public String[] createNames() {
        return new String[]{"playershopitem"};
    }
}