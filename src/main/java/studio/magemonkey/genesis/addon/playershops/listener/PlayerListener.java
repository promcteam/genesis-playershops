package studio.magemonkey.genesis.addon.playershops.listener;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.addon.playershops.managers.SaveManager.REASON_LOAD;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShop;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShopsUserInputPrice;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.GenesisShopHolder;
import studio.magemonkey.genesis.managers.ClassManager;

import java.io.File;

public class PlayerListener implements Listener {

    private final PlayerShops plugin;

    public PlayerListener(PlayerShops plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void joinServer(PlayerJoinEvent event) {
        join(event.getPlayer());
    }

    @EventHandler
    public void quitServer(PlayerQuitEvent event) {
        leave(event.getPlayer());
    }

    @EventHandler
    public void kickedOffServer(PlayerKickEvent event) {
        leave(event.getPlayer());
    }


    public void join(Player p) {
        if (plugin.getShopsManager() != null) {
            if (plugin.getSettings().getListOnlinePlayersOnly()
                    && plugin.getShopsManager().getPlayerShop(p.getUniqueId()) == null) {
                File file = new File(plugin.getGenesis().getDataFolder() + File.separator + "addons" +
                        File.separator + plugin.getAddonName() + File.separator + "shops" + File.separator +
                        p.getUniqueId().toString().charAt(0) + File.separator + p.getUniqueId().toString() +
                        ".yml");
                if (file.exists()) {
                    plugin.getSaveManager().loadShop(p.getUniqueId(), p, REASON_LOAD.OWNER_JOIN, false,
                            true);
                }
            }

            if (plugin.getShopsManager() != null) {
                PlayerShop shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
                if (shop != null) {
                    shop.ownerJoin(p);
                }
            }
        }

    }

    public void leave(Player p) {
        if (plugin.getShopsManager() != null) {
            PlayerShop shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
            if (shop != null) {
                shop.ownerLeave(p);
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void inventoryClick(InventoryClickEvent event) {
        if (event.isCancelled() && plugin.getSettings().getPreventSellingPluginsItems()) {
            return;
        }
        if (plugin.getShopsManager() != null) {
            if (event.getWhoClicked() instanceof Player) {
                Player p = (Player) event.getWhoClicked();
                if (plugin.getGenesis().getAPI().isValidShop(event.getInventory())) {

                    GenesisShopHolder holder = (GenesisShopHolder) event.getInventory().getHolder();
                    GenesisBuy        buy    = holder.getShopItem(event.getRawSlot());
                    if (buy != null) {
                        return;
                    }

                    if (event.getCurrentItem() == null) {
                        return;
                    }
                    if (event.getCurrentItem().getType() == Material.AIR) {
                        return;
                    }

                    PlayerShop shop = plugin.getShopsManager().getPlayerShop(p.getUniqueId());
                    if (shop != null) {
                        if (shop.isBeingEdited() && shop.getShopEdit()
                                == holder.getShop()) { //Player is in edit mode and clicked item of own inventory
                            event.setCancelled(true);
                            event.setResult(Result.DENY);
                            shopEditItemClick(p, shop, event.getCurrentItem(), event.getInventory(), event.getSlot(),
                                    ((GenesisShopHolder) event.getInventory().getHolder()));
                        } else if (shop.isBeingEdited() && plugin.getIconManager().getIconSelectionShop()
                                == holder.getShop()) { //Player is in edit mode and item selection menu and clicked item of own inventory
                            event.setCancelled(true);
                            event.setResult(Result.DENY);

                            if (plugin.getIconManager().getAllowInventoryItem(p, shop)) {
                                shopIconSelectionItemClick(p, shop, event.getCurrentItem(), holder);
                            } else {
                                ClassManager.manager.getMessageHandler().sendMessage("Main.NoPermission", p,
                                        null, p, plugin.getIconManager().getIconSelectionShop(), holder, null);
                            }
                        }
                    }
                }
            }
        }
    }

    public void shopEditItemClick(Player p,
                                  PlayerShop shop,
                                  ItemStack item,
                                  Inventory i,
                                  int slot,
                                  GenesisShopHolder holder) {
        if (p.getGameMode() == GameMode.CREATIVE && plugin.getSettings().getPreventCreativeAccess()) {
            plugin.getMessages().sendMessage("Message.PreventedCreativeAddItem", p, null, p,
                    shop.getShopEdit(), holder, null);
            return;
        }

        if (plugin.getBlacklist().isBlocked(item)) {
            plugin.getMessages().sendMessage("Message.InvalidItem", p, null, p, shop.getShopEdit(),
                    holder, null);
            return;
        }

        if (shop.containsItem(item)) {
            shop.increaseItemAmount(item, item.getAmount());
            p.getInventory().setItem(slot, null);
            plugin.getGenesis().getAPI().updateInventory(p);
        } else {

            if (!shop.isEmptySlotLeft(p)) {
                plugin.getMessages().sendMessage("Message.OutOfSlots", p, null, p, shop.getShopEdit(),
                        holder, null);
                return;
            }

            new PlayerShopsUserInputPrice(shop, p.getUniqueId(), item, slot).getUserInput(p,
                    plugin.getMessages().get("Message.EnterPrice"),
                    item,
                    plugin.getMessages().get("Message.EnterPrice"));
        }

    }

    public void shopIconSelectionItemClick(Player p, PlayerShop shop, ItemStack item, GenesisShopHolder holder) {
        shop.setIcon(item.clone(), true, true, true);
        plugin.getMessages().sendMessage("Message.IconSelected", p, null, p, holder.getShop(), holder, null);
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void inventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            if (plugin.getShopsManager() != null) {
                Player p = (Player) event.getPlayer();
                if (plugin.getGenesis().getAPI().isValidShop(event.getInventory())) {
                    if (plugin.getShopsManager() != null) {
                        GenesisShopHolder holder = (GenesisShopHolder) event.getInventory().getHolder();
                        PlayerShop        shop   = plugin.getShopsManager().getPlayerShop(holder.getShop(), false);
                        if (shop != null) {
                            shop.playerLeave(p);
                        }
                    }
                }
            }
        }
    }


}