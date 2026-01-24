package br.com.leonardson;

import javax.annotation.Nonnull;

import br.com.leonardson.database.DatabaseManager;
import br.com.leonardson.listeners.PlayerConnectListener;
import br.com.leonardson.listeners.PlayerDisconnectListener;
import br.com.leonardson.listeners.PlayerChatListener;
import br.com.leonardson.listeners.PlayerDeathEventSystem;
import br.com.leonardson.listeners.PlayerKillEventSystem;
import br.com.leonardson.listeners.PlayerBlockBreakEventSystem;
import br.com.leonardson.listeners.PlayerBlockPlaceEventSystem;
import br.com.leonardson.listeners.PlayerDistanceTraveledSystem;
import br.com.leonardson.listeners.PlayerItemDropEventSystem;
import br.com.leonardson.listeners.PlayerItemStatsListener;
import br.com.leonardson.commands.StatsCommand;
import br.com.leonardson.commands.TopStatsCommand;
import br.com.leonardson.ui.StatsHudSystem;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import java.util.logging.Level;

public class Main extends JavaPlugin {
    private static Main instance;
    private DatabaseManager databaseManager;
    private PlayerDisconnectListener playerDisconnectListener;
    private PlayerDistanceTraveledSystem distanceTraveledSystem;
    private StatsHudSystem statsHudSystem;

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        super.setup();
        getLogger().at(Level.INFO).log("Initializing TaleStatistics plugin");
        
        // Initialize database
        databaseManager = new DatabaseManager(this.getLogger());
        databaseManager.connect();

        // Initialize and register event listeners
        PlayerConnectListener playerConnectListener = new PlayerConnectListener(this, databaseManager);
        playerConnectListener.register();
        
        playerDisconnectListener = new PlayerDisconnectListener(this, databaseManager);
        playerDisconnectListener.register();
        
        PlayerChatListener playerChatListener = new PlayerChatListener(this, databaseManager);
        playerChatListener.register();
        
        getLogger().at(Level.INFO).log("Statistics event listeners registered successfully");

        // Register ECS death/kill event systems
        this.getEntityStoreRegistry().registerSystem(new PlayerDeathEventSystem(this, databaseManager));
        this.getEntityStoreRegistry().registerSystem(new PlayerKillEventSystem(this, databaseManager));
        this.getEntityStoreRegistry().registerSystem(new PlayerBlockBreakEventSystem(this, databaseManager));
        this.getEntityStoreRegistry().registerSystem(new PlayerBlockPlaceEventSystem(this, databaseManager));
        this.getEntityStoreRegistry().registerSystem(new PlayerItemDropEventSystem(this, databaseManager));
        distanceTraveledSystem = new PlayerDistanceTraveledSystem(this, databaseManager);
        this.getEntityStoreRegistry().registerSystem(distanceTraveledSystem);
        getLogger().at(Level.INFO).log("Death/Kill/Block/Item event systems registered successfully");

        PlayerItemStatsListener itemStatsListener = new PlayerItemStatsListener(this, databaseManager);
        itemStatsListener.register();

        statsHudSystem = new StatsHudSystem(this, databaseManager);
        this.getEntityStoreRegistry().registerSystem(statsHudSystem);

        // Register commands
        this.getCommandRegistry().registerCommand(new StatsCommand());
        this.getCommandRegistry().registerCommand(new TopStatsCommand());
        getLogger().at(Level.INFO).log("Statistics commands registered successfully");
    }

    @Override
    protected void shutdown() {
        super.shutdown();
        
        // Save playtime for all online players before shutdown
        if (playerDisconnectListener != null) {
            playerDisconnectListener.saveAllPlaytime();
            getLogger().at(Level.INFO).log("Final playtime save completed");
        }

        if (distanceTraveledSystem != null) {
            distanceTraveledSystem.flushAll();
            getLogger().at(Level.INFO).log("Final distance traveled save completed");
        }
        
        // Disconnect database on shutdown
        if (databaseManager != null && databaseManager.isConnected()) {
            databaseManager.disconnect();
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public PlayerDistanceTraveledSystem getDistanceTraveledSystem() {
        return distanceTraveledSystem;
    }

    public StatsHudSystem getStatsHudSystem() {
        return statsHudSystem;
    }
}
