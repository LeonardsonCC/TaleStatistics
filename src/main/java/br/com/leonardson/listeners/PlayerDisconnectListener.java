package br.com.leonardson.listeners;

import br.com.leonardson.Main;
import br.com.leonardson.database.DatabaseManager;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Listener for handling player disconnection and playtime tracking
 */
public class PlayerDisconnectListener {
    private final Main plugin;
    private final DatabaseManager database;
    
    // Track player session start times for playtime calculation
    private final Map<UUID, Long> sessionStartTimes = new HashMap<>();

    public PlayerDisconnectListener(Main plugin, DatabaseManager database) {
        this.plugin = plugin;
        this.database = database;
    }

    /**
     * Register the PlayerConnectEvent and PlayerDisconnectEvent listeners
     */
    public void register() {
        plugin.getEventRegistry().register(PlayerConnectEvent.class, this::onPlayerConnect);
        plugin.getEventRegistry().register(PlayerDisconnectEvent.class, this::onPlayerDisconnect);
        plugin.getLogger().at(Level.INFO).log("PlayerDisconnectListener registered");
    }

    /**
     * Handle player connection to start tracking session time
     */
    private void onPlayerConnect(PlayerConnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        sessionStartTimes.put(playerRef.getUuid(), System.currentTimeMillis());
    }

    /**
     * Handle player disconnection
     */
    private void onPlayerDisconnect(PlayerDisconnectEvent event) {
        PlayerRef playerRef = event.getPlayerRef();
        String uuid = playerRef.getUuid().toString();
        
        // Update playtime before disconnect
        updatePlaytime(playerRef.getUuid());

        if (plugin.getDistanceTraveledSystem() != null) {
            plugin.getDistanceTraveledSystem().onPlayerDisconnect(playerRef.getUuid());
        }
        
        // Remove from session tracking
        sessionStartTimes.remove(playerRef.getUuid());
        
        database.updateLastSeen(uuid);
        plugin.getLogger().at(Level.INFO).log("Player disconnected, stats saved");
    }
    
    /**
     * Updates playtime for a player
     */
    private void updatePlaytime(UUID uuid) {
        Long sessionStart = sessionStartTimes.get(uuid);
        if (sessionStart != null) {
            long sessionDuration = (System.currentTimeMillis() - sessionStart) / 1000; // Convert to seconds
            database.incrementStat(uuid.toString(), "playtime", (int) sessionDuration);
            // Reset the session start time for the next interval
            sessionStartTimes.put(uuid, System.currentTimeMillis());
        }
    }
    
    /**
     * Saves playtime for all currently tracked players
     * This is called periodically and on server shutdown
     */
    public void saveAllPlaytime() {
        for (UUID uuid : sessionStartTimes.keySet()) {
            updatePlaytime(uuid);
        }
    }
}
