package br.com.leonardson.listeners;

import br.com.leonardson.Main;
import br.com.leonardson.database.DatabaseManager;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.logging.Level;

/**
 * Listener for handling player connection events
 */
public class PlayerConnectListener {
    private final Main plugin;
    private final DatabaseManager database;

    public PlayerConnectListener(Main plugin, DatabaseManager database) {
        this.plugin = plugin;
        this.database = database;
    }

    /**
     * Register the PlayerConnectEvent listener
     */
    public void register() {
        plugin.getEventRegistry().register(PlayerConnectEvent.class, this::onPlayerConnect);
        plugin.getLogger().at(Level.INFO).log("PlayerConnectListener registered");
    }

    /**
     * Handle player connection
     */
    private void onPlayerConnect(PlayerConnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        String uuid = playerRef.getUuid().toString();
        String name = playerRef.getUsername();
        
        database.initializePlayer(uuid, name);
        database.updatePlayerName(uuid, name);
        plugin.getLogger().at(Level.INFO).log("Player " + name + " connected, initialized stats");
    }
}
