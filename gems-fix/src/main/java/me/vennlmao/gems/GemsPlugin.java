package me.vennlmao.gems;

import me.vennlmao.gems.command.GemCommand;
import me.vennlmao.gems.command.GemsCommand;
import me.vennlmao.gems.listener.GemUseListener;
import me.vennlmao.gems.listener.PlayerSessionListener;
import me.vennlmao.gems.manager.ConfigManager;
import me.vennlmao.gems.manager.GemRegistry;
import me.vennlmao.gems.manager.ManaManager;
import me.vennlmao.gems.resourcepack.PackBuilder;
import me.vennlmao.gems.resourcepack.PackListener;
import me.vennlmao.gems.resourcepack.PackServer;
import me.vennlmao.gems.scheduler.FoliaScheduler;
import org.bukkit.plugin.java.JavaPlugin;

public final class GemsPlugin extends JavaPlugin {

    private static GemsPlugin instance;
    private ConfigManager configManager;
    private GemRegistry gemRegistry;
    private ManaManager manaManager;
    private FoliaScheduler foliaScheduler;
    private PackServer packServer;
    private PackListener packListener;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        saveResource("gems.yml", false);

        foliaScheduler = new FoliaScheduler(this);
        configManager  = new ConfigManager(this);
        gemRegistry    = new GemRegistry(this);
        manaManager    = new ManaManager(this);
        packServer     = new PackServer(this);

        gemRegistry.loadAll();
        manaManager.startRegenTask();

        getServer().getPluginManager().registerEvents(new GemUseListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerSessionListener(this), this);

        getCommand("gem").setExecutor(new GemCommand(this));
        getCommand("gems").setExecutor(new GemsCommand(this));

        if (getConfig().getBoolean("resourcepack.enabled", true)) {
            foliaScheduler.runAsync(() -> {
                try {
                    new PackBuilder(this).buildIfNeeded();
                    packServer.start();
                    packListener = new PackListener(this, packServer);
                    foliaScheduler.runGlobal(() ->
                        getServer().getPluginManager().registerEvents(packListener, this));
                } catch (Exception e) {
                    getLogger().severe("Pack server failed to start: " + e.getMessage());
                }
            });
        }

        getLogger().info("Gems loaded — " + gemRegistry.size() + " gems active.");
    }

    @Override
    public void onDisable() {
        if (packServer != null) packServer.stop();
        foliaScheduler.cancelAll();
        if (manaManager != null) manaManager.cleanup();
        getLogger().info("Gems disabled.");
    }

    public static GemsPlugin get() { return instance; }
    public ConfigManager cfg()     { return configManager; }
    public GemRegistry registry()  { return gemRegistry; }
    public ManaManager mana()      { return manaManager; }
    public FoliaScheduler scheduler() { return foliaScheduler; }
    public PackServer packServer() { return packServer; }
    public PackListener packListener() { return packListener; }
}
