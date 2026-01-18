package br.com.leonardson.listeners;

import br.com.leonardson.Main;
import br.com.leonardson.database.DatabaseManager;
import com.hypixel.hytale.server.core.event.events.player.PlayerChatEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.logging.Level;

/**
 * Listener for handling player chat events and tracking messages sent
 */
public class PlayerChatListener {
    private final Main plugin;
    private final DatabaseManager database;

    public PlayerChatListener(Main plugin, DatabaseManager database) {
        this.plugin = plugin;
        this.database = database;
    }

    /**
     * Register the PlayerChatEvent listener
     */
    public void register() {
        // Chat events - PlayerChatEvent implements IAsyncEvent<String>
        // The event is dispatched globally without a specific key, so we use registerAsyncGlobal
        plugin.getEventRegistry().<String, PlayerChatEvent>registerAsyncGlobal(PlayerChatEvent.class, future -> 
            future.thenApply(event -> {
                onChat(event);
                return event;
            })
        );
        plugin.getLogger().at(Level.FINE).log("PlayerChatListener registered (async global)");
    }

    /**
     * Handle chat messages
     */
    private void onChat(PlayerChatEvent event) {
        if (event.isCancelled()) {
            plugin.getLogger().at(Level.FINE).log("Chat event was cancelled");
            return;
        }
        
        PlayerRef sender = event.getSender();
        if (sender != null) {
            String uuid = sender.getUuid().toString();
            database.incrementStat(uuid, "messages_sent", 1);
            plugin.getLogger().at(Level.FINE).log("Message count incremented for player: " + sender.getUsername());
        } else {
            plugin.getLogger().at(Level.WARNING).log("Chat event sender was null");
        }
    }
}
