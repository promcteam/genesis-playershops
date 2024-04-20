package studio.magemonkey.genesis.addon.playershops.managers;

import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.addon.playershops.objects.PlayerShop;
import studio.magemonkey.genesis.managers.ClassManager;

@RequiredArgsConstructor
public class CommandManager implements CommandExecutor {
    private final PlayerShops plugin;

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can not be used by the console.");
            return false;
        }

        Player p = (Player) sender;

        if (plugin.getShopsManager() == null) {
            return false;
        }

        if (!(p.hasPermission("PlayerShops.open") | !plugin.getSettings().getPermissionsEnabled())) {
            ClassManager.manager.getMessageHandler().sendMessage("Main.NoPermission", p);
            return false;
        }

        if (args.length == 0) {
            plugin.getShopsManager().openShoplist(p);
            return true;
        }

        String target = args[0];
        return tryOpenShop(p, target, true);
    }


    public boolean tryOpenShop(Player p, String target, boolean failMessage) {
        if (plugin.getShopsManager() == null) return false;

        Player     t = Bukkit.getPlayer(target);
        PlayerShop shop;

        if (t != null) {
            shop = plugin.getShopsManager().getPlayerShop(t.getUniqueId());
        } else {
            shop = plugin.getShopsManager().getPlayerShop(target);
        }

        if (shop == null) {
            plugin.getMessages().sendMessage("Message.ShopNotFound", p, target);
            return false;
        }

        if (shop.getShop() == null || (shop.isBeingEdited() && !shop.getOwner().equals(p.getUniqueId()))) {
            plugin.getMessages()
                    .sendMessage("Message.ShopBeingEdited", p, target, t, shop.getShop(), null, null);
            return false;
        }

        shop.getShop().openInventory(p);
        return true;
    }
}
