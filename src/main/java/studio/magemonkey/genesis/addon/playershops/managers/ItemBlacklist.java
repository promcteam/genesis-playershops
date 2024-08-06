package studio.magemonkey.genesis.addon.playershops.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import studio.magemonkey.genesis.managers.ClassManager;
import studio.magemonkey.genesis.managers.item.ItemStackChecker;
import studio.magemonkey.genesis.managers.misc.InputReader;

import java.util.List;

public class ItemBlacklist {
    private final List<ItemStack> items;

    public ItemBlacklist(FileConfiguration config) {
        items = InputReader.readItemList(config.get("ForbiddenItems"), true);
        for (ItemStack item : items) {
            item.setAmount(1);
        }
    }

    public boolean isBlocked(ItemStack check) {
        ItemStackChecker c = ClassManager.manager.getItemStackChecker();
        for (ItemStack item : items) {
            if (c.isEqualShopItemAdvanced(item, check, true, false, false, null)) {
                return true;
            }
        }
        return false;
    }
}
