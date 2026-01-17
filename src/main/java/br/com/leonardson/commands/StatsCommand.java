package br.com.leonardson.commands;

import br.com.leonardson.Main;
import br.com.leonardson.ui.StatsPage;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class StatsCommand extends AbstractPlayerCommand {
    private final OptionalArg<String> targetPlayerArg = this.withOptionalArg(
            "player",
            "Player name to view stats for",
            ArgTypes.STRING
    );

    public StatsCommand() {
        super("stats", "View player statistics", false);
    }

    @Override
    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef,
            World world) {
        String uuid = playerRef.getUuid().toString();
        String playerName = playerRef.getUsername();
        boolean usingTarget = false;

        try {
            ResultSet rs;
            if (targetPlayerArg.provided(context)) {
                usingTarget = true;
                String targetName = targetPlayerArg.get(context).trim();
                if (targetName.isEmpty()) {
                    playerRef.sendMessage(Message.raw("Please provide a valid player name."));
                    return;
                }
                rs = Main.getInstance().getDatabaseManager().getPlayerStatsByName(targetName);
            } else {
                rs = Main.getInstance().getDatabaseManager().getPlayerStats(uuid);
            }

            if (rs != null && rs.next()) {
                playerName = rs.getString("player_name");
                int kills = rs.getInt("kills");
                int mobKills = rs.getInt("mob_kills");
                int deaths = rs.getInt("deaths");
                int blocksPlaced = rs.getInt("blocks_placed");
                int blocksBroken = rs.getInt("blocks_broken");
                int itemsDropped = rs.getInt("items_dropped");
                int itemsPickedUp = rs.getInt("items_picked_up");
                int messagesSent = rs.getInt("messages_sent");
                String distanceTraveled = String.format("%.2f blocks", rs.getDouble("distance_traveled"));
                String playtime = formatPlaytime(rs.getInt("playtime"));

                Player playerComponent = store.getComponent(ref, Player.getComponentType());
                if (playerComponent == null) {
                    playerRef.sendMessage(Message.raw("Unable to open stats UI right now."));
                    rs.close();
                    return;
                }

                StatsPage page = new StatsPage(
                        playerRef,
                        playerName,
                        kills,
                        mobKills,
                        deaths,
                        blocksPlaced,
                        blocksBroken,
                        itemsDropped,
                        itemsPickedUp,
                        messagesSent,
                        distanceTraveled,
                        playtime
                );
                playerComponent.getPageManager().openCustomPage(ref, store, page);
                rs.close();
            } else {
                if (rs != null) {
                    rs.close();
                }
                if (usingTarget) {
                    playerRef.sendMessage(Message.raw("No statistics found for that player."));
                } else {
                    playerRef.sendMessage(Message.raw("No statistics found. Try playing for a bit!"));
                }
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

