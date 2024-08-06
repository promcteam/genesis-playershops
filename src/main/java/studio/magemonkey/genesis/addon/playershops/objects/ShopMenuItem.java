package studio.magemonkey.genesis.addon.playershops.objects;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.conditions.GenesisCondition;
import studio.magemonkey.genesis.core.prices.GenesisPriceType;
import studio.magemonkey.genesis.core.rewards.GenesisRewardType;
import studio.magemonkey.genesis.managers.ClassManager;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ShopMenuItem {
    private final String       path;
    private final String       message;
    private final List<String> itemData;

    private ItemStack itemStack;

    public ShopMenuItem(FileConfiguration config, String path) {
        this(path, config.getStringList(path + ".MenuItem"), config.getString(path + ".Message"));
    }

    public ShopMenuItem(String path, List<String> itemData, String message) {
        this.path = path;
        this.itemData = itemData;
        if (itemData != null) {
            if (!itemData.isEmpty()) {
                this.itemStack = ClassManager.manager.getItemStackCreator().createItemStack(itemData, false);
            }
        }
        this.message = message;
    }


    public List<ItemStack> getItemList(int amount) {
        if (itemStack != null) {
            ArrayList<ItemStack> list = new ArrayList<ItemStack>();
            ItemStack            item = itemStack.clone();
            item.setAmount(amount);
            list.add(item);
            return list;
        }
        return null;
    }


    public GenesisBuy createShopItem(GenesisRewardType rewardType,
                                     GenesisPriceType priceType,
                                     Object reward,
                                     Object price,
                                     int inventoryLocation,
                                     String permission) {
        return createShopItem(rewardType, priceType, reward, price, inventoryLocation, permission, null);
    }

    public GenesisBuy createShopItem(GenesisRewardType rewardType,
                                     GenesisPriceType priceType,
                                     Object reward,
                                     Object price,
                                     int inventoryLocation,
                                     String permission,
                                     GenesisCondition condition) {
        return createShopItem(rewardType, priceType, reward, price, message, inventoryLocation, permission, condition);
    }

    public GenesisBuy createShopItem(GenesisRewardType rewardType,
                                     GenesisPriceType priceType,
                                     Object reward,
                                     Object price,
                                     String message,
                                     int inventoryLocation,
                                     String permission,
                                     GenesisCondition condition) {
        if (itemStack != null) {
            GenesisBuy buy = new GenesisBuy(rewardType,
                    priceType,
                    reward,
                    price,
                    message,
                    inventoryLocation,
                    permission,
                    path,
                    condition,
                    null,
                    null);
            buy.setItem(itemStack, false);
            return buy;
        } else {
            return null;
        }
    }
}
