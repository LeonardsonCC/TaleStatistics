package br.com.leonardson.commands;

import br.com.leonardson.Main;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class StatsCommand extends AbstractPlayerCommand {
    public StatsCommand() {
        super("stats", "View your statistics", false);
    }

    @Override
    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef,
            World world) {
        String uuid = playerRef.getUuid().toString();
        String playerName = playerRef.getUsername();

        try {
            ResultSet rs = Main.getInstance().getDatabaseManager().getPlayerStats(uuid);
            
            if (rs != null && rs.next()) {
                // Format statistics display
                playerRef.sendMessage(Message.raw("========== Statistics for " + playerName + " =========="));
                playerRef.sendMessage(Message.raw(""));
                
                // Combat stats
                playerRef.sendMessage(Message.raw("Combat:"));
                playerRef.sendMessage(Message.raw("  Player Kills: " + rs.getInt("kills")));
                playerRef.sendMessage(Message.raw("  Mob Kills: " + rs.getInt("mob_kills")));
                playerRef.sendMessage(Message.raw("  Deaths: " + rs.getInt("deaths")));
                
                // Building stats
                playerRef.sendMessage(Message.raw(""));
                playerRef.sendMessage(Message.raw("Building:"));
                playerRef.sendMessage(Message.raw("  Blocks Placed: " + rs.getInt("blocks_placed")));
                playerRef.sendMessage(Message.raw("  Blocks Broken: " + rs.getInt("blocks_broken")));
                
                // Item stats
                playerRef.sendMessage(Message.raw(""));
                playerRef.sendMessage(Message.raw("Items:"));
                playerRef.sendMessage(Message.raw("  Items Crafted: " + rs.getInt("items_crafted")));
                playerRef.sendMessage(Message.raw("  Items Dropped: " + rs.getInt("items_dropped")));
                playerRef.sendMessage(Message.raw("  Items Picked Up: " + rs.getInt("items_picked_up")));
                
                // Social stats
                playerRef.sendMessage(Message.raw(""));
                playerRef.sendMessage(Message.raw("Social:"));
                playerRef.sendMessage(Message.raw("  Messages Sent: " + rs.getInt("messages_sent")));
                
                // General stats
                playerRef.sendMessage(Message.raw(""));
                playerRef.sendMessage(Message.raw("General:"));
                playerRef.sendMessage(Message.raw("  Distance Traveled: " + String.format("%.2f", rs.getDouble("distance_traveled")) + " blocks"));
                playerRef.sendMessage(Message.raw("  Playtime: " + formatPlaytime(rs.getInt("playtime"))));
                
                playerRef.sendMessage(Message.raw(""));
                playerRef.sendMessage(Message.raw("============================================="));
                
                rs.close();
            } else {
                playerRef.sendMessage(Message.raw("No statistics found. Try playing for a bit!"));
            }
        } catch (SQLException e) {
            playerRef.sendMessage(Message.raw("Error retrieving statistics: " + e.getMessage()));
            Main.getInstance().getLogger().at(Level.SEVERE).log("Error in stats command: " + e.getMessage());
        }
    }
    
    /**
     * Format playtime in a human-readable format
     */
    private String formatPlaytime(int seconds) {
        if (seconds < 60) {
            return seconds + " seconds";
        }
        
        int minutes = seconds / 60;
        if (minutes < 60) {
            return minutes + " minutes";
        }
        
        int hours = minutes / 60;
        minutes = minutes % 60;
        
        if (hours < 24) {
            return hours + "h " + minutes + "m";
        }
        
        int days = hours / 24;
        hours = hours % 24;
        
        return days + "d " + hours + "h " + minutes + "m";
    }
}

