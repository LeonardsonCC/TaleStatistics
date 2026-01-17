package br.com.leonardson.commands;

import br.com.leonardson.Main;
import br.com.leonardson.ui.TopStatsPage;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Level;

public class TopStatsCommand extends AbstractPlayerCommand {
    private final RequiredArg<String> statArg = this.withRequiredArg(
            "stat",
            "Stat to rank by",
            ArgTypes.STRING
    );

    private static final Map<String, String> STAT_ALIASES = Map.ofEntries(
            Map.entry("kills", "kills"),
            Map.entry("kill", "kills"),
            Map.entry("mob_kills", "mob_kills"),
            Map.entry("mobkills", "mob_kills"),
            Map.entry("deaths", "deaths"),
            Map.entry("death", "deaths"),
            Map.entry("blocks_placed", "blocks_placed"),
            Map.entry("blocksplaced", "blocks_placed"),
            Map.entry("blocks_broken", "blocks_broken"),
            Map.entry("blocksbroken", "blocks_broken"),
            Map.entry("blocks_damaged", "blocks_damaged"),
            Map.entry("blocksdamaged", "blocks_damaged"),
            Map.entry("blocks_used", "blocks_used"),
            Map.entry("blocksused", "blocks_used"),
            Map.entry("items_dropped", "items_dropped"),
            Map.entry("itemsdropped", "items_dropped"),
            Map.entry("items_picked_up", "items_picked_up"),
            Map.entry("itemspickedup", "items_picked_up"),
            Map.entry("items_crafted", "items_crafted"),
            Map.entry("itemscrafted", "items_crafted"),
            Map.entry("messages_sent", "messages_sent"),
            Map.entry("messagessent", "messages_sent"),
            Map.entry("distance_traveled", "distance_traveled"),
            Map.entry("distancetraveled", "distance_traveled"),
            Map.entry("playtime", "playtime")
    );

    public TopStatsCommand() {
        super("topstats", "View the top 10 players for a stat", false);
    }

    @Override
    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef,
            World world) {
        String rawStat = statArg.get(context).trim();
        if (rawStat.isEmpty()) {
            playerRef.sendMessage(Message.raw("Please provide a stat name."));
            sendValidStats(playerRef);
            return;
        }

        String normalized = normalizeStat(rawStat);
        if (normalized == null) {
            playerRef.sendMessage(Message.raw("Unknown stat: " + rawStat));
            sendValidStats(playerRef);
            return;
        }

        try {
            ResultSet rs = Main.getInstance().getDatabaseManager().getTopPlayersByStat(normalized, 10);
            if (rs == null) {
                playerRef.sendMessage(Message.raw("Unable to load top stats right now."));
                return;
            }

            String displayName = displayNameFor(normalized);

            Player playerComponent = store.getComponent(ref, Player.getComponentType());
            if (playerComponent == null) {
                playerRef.sendMessage(Message.raw("Unable to open top stats UI right now."));
                rs.close();
                return;
            }

            java.util.List<String> entries = new java.util.ArrayList<>(10);
            int rank = 1;
            while (rs.next()) {
                String playerName = rs.getString("player_name");
                String value = formatValue(normalized, rs);
                entries.add(rank + ". " + playerName + " - " + value);
                rank++;
            }

            rs.close();

            if (entries.isEmpty()) {
                playerRef.sendMessage(Message.raw("No statistics found yet."));
                return;
            }

            TopStatsPage page = new TopStatsPage(playerRef, displayName, entries);
            playerComponent.getPageManager().openCustomPage(ref, store, page);
        } catch (SQLException e) {
            playerRef.sendMessage(Message.raw("Error retrieving top stats: " + e.getMessage()));
            Main.getInstance().getLogger().at(Level.SEVERE).log("Error in topstats command: " + e.getMessage());
        }
    }

    private String normalizeStat(String raw) {
        String normalized = raw.toLowerCase().trim().replace("-", "_").replace(" ", "_");
        return STAT_ALIASES.get(normalized);
    }

    private String displayNameFor(String statColumn) {
        return switch (statColumn) {
            case "mob_kills" -> "Mob Kills";
            case "blocks_placed" -> "Blocks Placed";
            case "blocks_broken" -> "Blocks Broken";
            case "blocks_damaged" -> "Blocks Damaged";
            case "blocks_used" -> "Blocks Used";
            case "items_dropped" -> "Items Dropped";
            case "items_picked_up" -> "Items Picked Up";
            case "items_crafted" -> "Items Crafted";
            case "messages_sent" -> "Messages Sent";
            case "distance_traveled" -> "Distance Traveled";
            case "playtime" -> "Playtime";
            default -> capitalize(statColumn);
        };
    }

    private String formatValue(String statColumn, ResultSet rs) throws SQLException {
        if ("distance_traveled".equals(statColumn)) {
            return String.format("%.2f blocks", rs.getDouble(statColumn));
        }
        if ("playtime".equals(statColumn)) {
            return formatPlaytime(rs.getInt(statColumn));
        }
        return String.valueOf(rs.getInt(statColumn));
    }

    private void sendValidStats(PlayerRef playerRef) {
        playerRef.sendMessage(Message.raw("Valid stats: kills, mob_kills, deaths, blocks_placed, blocks_broken, blocks_damaged, blocks_used, items_dropped, items_picked_up, items_crafted, messages_sent, distance_traveled, playtime"));
    }

    private String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

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
