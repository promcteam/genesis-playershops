package studio.magemonkey.genesis.addon.playershops.objects;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import studio.magemonkey.genesis.managers.ClassManager;
import studio.magemonkey.genesis.misc.userinput.GenesisUserInput;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerShopsUserInputRename extends GenesisUserInput {
    private final PlayerShop shop;
    private final UUID       uuid;

    public void receivedInput(Player p, String text) {
        text = text.replaceAll(String.valueOf(ChatColor.COLOR_CHAR), "&");
        if (p.getUniqueId() == uuid) { //probably it is not even possible this event will trigger with an other player
            if (!shop.getPlugin().getIconManager().getAllowShopRenameColors(p, shop)) {
                text = text.replaceAll("&", "");
            }


            text = ClassManager.manager.getStringManager().transform(text, null, shop.getCurrentShop(), null, p);
            ItemStack i = shop.getIcon();
            if (i != null && i.hasItemMeta()) {
                ItemMeta meta = i.getItemMeta();
                if (meta.hasDisplayName()) {
                    meta.setDisplayName(meta.getDisplayName().replace(shop.getShopDisplayName(), text));
                    i.setItemMeta(meta);
                }
            }
            shop.setShopDisplayName(text);
            shop.updateIcon(p);
            shop.getShopEdit().openInventory(p);

        }
    }
}
