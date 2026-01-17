package br.com.leonardson;

import javax.annotation.Nonnull;

import br.com.leonardson.database.DatabaseManager;
import br.com.leonardson.listeners.PlayerConnectListener;
import br.com.leonardson.listeners.PlayerDisconnectListener;
import br.com.leonardson.listeners.PlayerChatListener;
import br.com.leonardson.listeners.PlayerDeathEventSystem;
import br.com.leonardson.listeners.PlayerKillEventSystem;
import br.com.leonardson.commands.StatsCommand;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import java.util.logging.Level;

public class Main extends JavaPlugin {
    private static Main instance;
    private DatabaseManager databaseManager;
    private PlayerDisconnectListener playerDisconnectListener;

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        super.setup();
        System.out.println("[HYTALE STATISTICS] Setup method called!");
        
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
        getLogger().at(Level.INFO).log("Death/Kill event systems registered successfully");

        // Register commands
        this.getCommandRegistry().registerCommand(new StatsCommand());
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
}
