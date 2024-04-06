package studio.magemonkey.genesis.addon.playershops.managers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import studio.magemonkey.genesis.core.GenesisBuy;
import studio.magemonkey.genesis.core.GenesisShop;
import studio.magemonkey.genesis.core.GenesisShopHolder;
import studio.magemonkey.genesis.managers.ClassManager;

@Getter
@RequiredArgsConstructor
public class MessageHandler {
    private final FileConfiguration config;

    public void sendMessage(String node, CommandSender sender) {
        sendMessage(node, sender, null, null, null, null, null);
    }

    public void sendMessage(String node, CommandSender sender, String offline_target) {
        sendMessage(node, sender, offline_target, null, null, null, null);
    }

    public void sendMessage(String node, CommandSender sender, Player target) {
        sendMessage(node, sender, null, target, null, null, null);
    }

    public void sendMessage(String node,
                            CommandSender sender,
                            String offline_target,
                            Player target,
                            GenesisShop shop,
                            GenesisShopHolder holder,
                            GenesisBuy item) {
        if (sender != null) {

            if (node == null || node.equals("")) {
                return;
            }

            String message = get(node, target, shop, holder, item);

            if (message == null || message.length() < 2) {
                return;
            }

            if (offline_target != null) {
                message = message.replace("%player%", offline_target)
                        .replace("%name%", offline_target)
                        .replace("%target%", offline_target);
            }

            sendMessageDirect(message, sender);
        }
    }

    public void sendMessageDirect(String message, CommandSender sender) {
        if (sender != null) {

            if (message == null || message.length() < 2) {
                return;
            }

            for (String line : message.split("\n"))
                sender.sendMessage(line);
        }
    }


    public String get(String node) {
        return get(node, null, null, null, null);
    }

    private String get(String node, Player target, GenesisShop shop, GenesisShopHolder holder, GenesisBuy item) {
        return replace(config.getString(node, node), target, shop, holder, item);
    }

    private String replace(String message, Player target, GenesisShop shop, GenesisShopHolder holder, GenesisBuy item) {
        return ClassManager.manager.getStringManager().transform(message, item, shop, holder, target);
    }


}