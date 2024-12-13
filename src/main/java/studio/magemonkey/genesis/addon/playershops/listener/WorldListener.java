package studio.magemonkey.genesis.addon.playershops.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldSaveEvent;
import studio.magemonkey.genesis.addon.playershops.PlayerShops;
import studio.magemonkey.genesis.addon.playershops.managers.SaveManager.REASON_SAVE;

public class WorldListener implements Listener {

    private final PlayerShops plugin;
    private       long        latest;

    public WorldListener(PlayerShops plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void register(WorldSaveEvent event) {
        if (latest + 5000 < System.currentTimeMillis()) {
            plugin.getShopsManager().save(plugin, REASON_SAVE.WORLD_SAVE);
            latest = System.currentTimeMillis();
        }
    }


}
