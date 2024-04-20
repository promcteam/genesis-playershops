package studio.magemonkey.genesis.addon.playershops;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import studio.magemonkey.genesis.addon.playershops.listener.GenesisListener;
import studio.magemonkey.genesis.addon.playershops.listener.PlayerListener;
import studio.magemonkey.genesis.addon.playershops.listener.SignListener;
import studio.magemonkey.genesis.addon.playershops.managers.*;
import studio.magemonkey.genesis.addon.playershops.managers.SaveManager.REASON_SAVE;
import studio.magemonkey.genesis.api.GenesisAddonConfig;
import studio.magemonkey.genesis.api.GenesisAddonConfigurable;
import studio.magemonkey.genesis.managers.config.FileHandler;

public class PlayerShops extends GenesisAddonConfigurable {
    @Getter
    private PlayerShopsManager shopsManager;
    @Getter
    private Settings           settings;
    @Getter
    private ShopMenuItems      items;
    @Getter
    private GenesisListener    genesisListener;
    @Getter
    private SignListener       signListener;
    @Getter
    private CustomActions      actions;
    @Getter
    private CommandManager     commandManager;
    @Getter
    private SaveManager        saveManager;
    @Getter
    private ShopCreator        shopCreator;
    @Getter
    private ShopIconManager    iconManager;
    @Getter
    private MessageHandler     messages;
    @Getter
    private ItemBlacklist      blacklist;

    private PlayerListener playerListener;

    @Override
    public void enableAddon() {
        shopCreator = new ShopCreator();
        genesisListener = new GenesisListener(this);
        Bukkit.getPluginManager().registerEvents(genesisListener, this);
        playerListener = new PlayerListener(this);
        Bukkit.getPluginManager().registerEvents(playerListener, this);
        signListener = new SignListener(this);
        Bukkit.getPluginManager().registerEvents(signListener, this);
        commandManager = new CommandManager(this);
        getCommand("ps").setExecutor(commandManager);
        getCommand("psadmin").setExecutor(new CommandManagerAdmin(this));
        saveManager = new SaveManager(this);
    }

    @Override
    public void genesisFinishedLoading() { //Executed on reload too
        load();
    }

    @Override
    public void disableAddon() {
        shopsManager.save(this, REASON_SAVE.SERVER_UNLOAD);
        shopsManager = null;
    }

    @Override
    public void genesisReloaded(CommandSender s) {
        shopsManager.save(this, REASON_SAVE.SERVER_RELOAD);
    }

    public void load() {
        new FileHandler().copyDefaultsFromJar(this, "config.yml");
        new FileHandler().copyDefaultsFromJar(this, "messages.yml");
        GenesisAddonConfig iconsconfig = new GenesisAddonConfig(this, "icons");
        if (!iconsconfig.getFile().exists()) {
            new FileHandler().copyDefaultsFromJar(this,
                    "look.yml"); //create once only because buttons can be removed from file
            new FileHandler().copyDefaultsFromJar(this, "icons.yml");
            iconsconfig.reload();
        }
        new FileHandler().copyDefaultsFromJar(this, "itemblacklist.yml");
        reloadConfig();
        messages = new MessageHandler(new GenesisAddonConfig(this, "messages").getConfig());
        blacklist = new ItemBlacklist(new GenesisAddonConfig(this, "itemblacklist").getConfig());
        settings = new Settings(getConfig());
        items = new ShopMenuItems(new GenesisAddonConfig(this, "look").getConfig());
        iconManager = new ShopIconManager(getConfig(), iconsconfig.getConfig());
        actions = new CustomActions(this);
        shopsManager = new PlayerShopsManager();
        shopsManager.init(this);
        iconManager.setupIconShop(this);
    }

    @Override
    public String getAddonName() {
        return "PlayerShops";
    }

    @Override
    public String getRequiredGenesisVersion() {
        return "1.0.0";
    }

    @Override
    public boolean saveConfigOnDisable() {
        return false;
    }
}
