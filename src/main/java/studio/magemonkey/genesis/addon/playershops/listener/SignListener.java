package studio.magemonkey.genesis.addon.playershops.listener;

import lombok.RequiredArgsConstructor;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.managers.ClassManager;

@RequiredArgsConstructor
public class SignListener implements Listener {
    private final PlayerShops plugin;

    @EventHandler
    public void createSign(SignChangeEvent e) {
        if (!plugin.getSettings().getSignsEnabled() || plugin.getShopsManager() == null) {
            return;
        }

        String text = e.getLine(0).toLowerCase();

        boolean playerShop  = text.endsWith(plugin.getSettings().getSignsTextPlayerShop().toLowerCase());
        boolean shopListing = text.endsWith(plugin.getSettings().getSignsTextShopListing().toLowerCase());
        if (playerShop || shopListing) {
            if (plugin.getSettings().getPermissionsEnabled()) {
                String line2 = ChatColor.stripColor(e.getLine(1));
                Player p     = e.getPlayer();
                if (!p.hasPermission("PlayerShops.createSign")
                        || (playerShop & !line2.equalsIgnoreCase(p.getName()) & !p.hasPermission(
                        "PlayerShops.createSign.other"))) {
                    ClassManager.manager.getMessageHandler().sendMessage("Main.NoPermission", e.getPlayer());
                    e.setCancelled(true);
                    return;
                }
            }

            if (!e.getLine(0).equals("")) {
                e.setLine(0, ClassManager.manager.getStringManager().transform(e.getLine(0)));
            }
            if (!e.getLine(1).equals("")) {
                e.setLine(1, ClassManager.manager.getStringManager().transform(e.getLine(1)));
            }
            if (!e.getLine(2).equals("")) {
                e.setLine(2, ClassManager.manager.getStringManager().transform(e.getLine(2)));
            }
            if (!e.getLine(3).equals("")) {
                e.setLine(3, ClassManager.manager.getStringManager().transform(e.getLine(3)));
            }
        }

    }


    @EventHandler
    public void interactSign(PlayerInteractEvent e) {
        if (plugin.getSettings() == null || plugin.getShopsManager() == null) {
            return;
        }
        if (!plugin.getSettings().getSignsEnabled() || e.getClickedBlock() == null
                || e.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block b = e.getClickedBlock();
        if (b.getType().name().contains("SIGN") && b.getState() instanceof Sign) {
            Sign s = (Sign) b.getState();

            String text = s.getLine(0).toLowerCase();
            if (text.endsWith(plugin.getSettings().getSignsTextPlayerShop().toLowerCase())) {
                plugin.getCommandManager()
                        .tryOpenShop(e.getPlayer(), ChatColor.stripColor(s.getLine(1)), true);
            } else if (text.endsWith(plugin.getSettings().getSignsTextShopListing().toLowerCase())) {
                plugin.getShopsManager().openShoplist(e.getPlayer());
            }
        }
    }
}
