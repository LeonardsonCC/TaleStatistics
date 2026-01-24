package br.com.leonardson.ui;

import br.com.leonardson.Main;
import br.com.leonardson.database.DatabaseManager;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class StatsHudSystem extends EntityTickingSystem<EntityStore> {
    private static final long UPDATE_INTERVAL_MS = 5000L;
    private static final String MULTI_HUD_CLASS = "com.buuz135.mhud.MultipleHUD";
    private static final String MULTI_HUD_KEY = "TaleStatistics";

    @Nonnull
    private final ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
    @Nonnull
    private final ComponentType<EntityStore, Player> playerComponentType = Player.getComponentType();

    private final Main plugin;
    private final DatabaseManager database;
    private final Map<UUID, StatsHud> huds = new HashMap<>();
    private final Map<UUID, Long> lastUpdates = new HashMap<>();
    private final Set<UUID> disabledHud = new HashSet<>();
    private boolean multipleHudAvailable;
    private Object multipleHudInstance;
    private Method multipleHudSetMethod;
    private Method multipleHudHideMethod;

    public StatsHudSystem(@Nonnull Main plugin, @Nonnull DatabaseManager database) {
        this.plugin = plugin;
        this.database = database;
        initializeMultipleHudSupport();
    }

    @Override
    public boolean isParallel(int archetypeChunkSize, int taskCount) {
        return false;
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer
    ) {
        PlayerRef playerRef = archetypeChunk.getComponent(index, playerRefComponentType);
        Player player = archetypeChunk.getComponent(index, playerComponentType);
        if (playerRef == null || player == null) {
            return;
        }

        UUID uuid = playerRef.getUuid();
        StatsHud hud = huds.get(uuid);
        if (hud == null) {
            hud = new StatsHud(playerRef);
            huds.put(uuid, hud);
            registerHud(playerRef, player, hud);
            lastUpdates.put(uuid, 0L);
        }

        if (disabledHud.contains(uuid)) {
            if (hud.isVisible()) {
                hud.setVisible(false);
            }
            return;
        }

        if (!hud.isVisible()) {
            hud.setVisible(true);
        }

        long now = System.currentTimeMillis();
        long lastUpdate = lastUpdates.getOrDefault(uuid, 0L);
        if (now - lastUpdate < UPDATE_INTERVAL_MS) {
            return;
        }

        lastUpdates.put(uuid, now);
        refreshStats(uuid, hud);
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.of(playerRefComponentType, playerComponentType);
    }

    public void onPlayerDisconnect(@Nonnull UUID uuid) {
        huds.remove(uuid);
        lastUpdates.remove(uuid);
        disabledHud.remove(uuid);
    }

    public boolean isHudEnabled(@Nonnull UUID uuid) {
        return !disabledHud.contains(uuid);
    }

    public void setHudEnabled(@Nonnull PlayerRef playerRef, @Nonnull Player player, boolean enabled) {
        UUID uuid = playerRef.getUuid();
        if (enabled) {
            disabledHud.remove(uuid);
            StatsHud hud = huds.get(uuid);
            if (hud == null) {
                hud = new StatsHud(playerRef);
                huds.put(uuid, hud);
            }
            registerHud(playerRef, player, hud);
            if (!hud.isVisible()) {
                hud.setVisible(true);
            }
            lastUpdates.put(uuid, 0L);
            refreshStats(uuid, hud);
        } else {
            disabledHud.add(uuid);
            StatsHud hud = huds.get(uuid);
            if (hud != null && hud.isVisible()) {
                hud.setVisible(false);
            }
            hideHud(playerRef, player);
        }
    }

    private void initializeMultipleHudSupport() {
        try {
            Class<?> multiHudClass = Class.forName(MULTI_HUD_CLASS);
            Method getInstanceMethod = multiHudClass.getMethod("getInstance");
            Object instance = getInstanceMethod.invoke(null);
            if (instance == null) {
                return;
            }
            multipleHudSetMethod = multiHudClass.getMethod(
                    "setCustomHud",
                    Player.class,
                    PlayerRef.class,
                    String.class,
                    CustomUIHud.class
            );
            multipleHudHideMethod = multiHudClass.getMethod(
                    "hideCustomHud",
                    Player.class,
                    PlayerRef.class,
                    String.class
            );
            multipleHudInstance = instance;
            multipleHudAvailable = true;
            plugin.getLogger().at(Level.INFO).log("MultipleHUD detected, registering HUDs through it");
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private void registerHud(@Nonnull PlayerRef playerRef, @Nonnull Player player, @Nonnull StatsHud hud) {
        if (multipleHudAvailable && multipleHudInstance != null && multipleHudSetMethod != null) {
            try {
                multipleHudSetMethod.invoke(multipleHudInstance, player, playerRef, MULTI_HUD_KEY, hud);
                return;
            } catch (ReflectiveOperationException e) {
                plugin.getLogger().at(Level.WARNING).log("Failed to register HUD with MultipleHUD, falling back");
                multipleHudAvailable = false;
            }
        }
        player.getHudManager().setCustomHud(playerRef, hud);
    }

    private void hideHud(@Nonnull PlayerRef playerRef, @Nonnull Player player) {
        if (multipleHudAvailable && multipleHudInstance != null && multipleHudHideMethod != null) {
            try {
                multipleHudHideMethod.invoke(multipleHudInstance, player, playerRef, MULTI_HUD_KEY);
                return;
            } catch (ReflectiveOperationException e) {
                plugin.getLogger().at(Level.WARNING).log("Failed to hide HUD with MultipleHUD, falling back");
                multipleHudAvailable = false;
            }
        }
    }

    private void refreshStats(@Nonnull UUID uuid, @Nonnull StatsHud hud) {
        ResultSet rs = null;
        try {
            rs = database.getPlayerStats(uuid.toString());
            if (rs != null && rs.next()) {
                int kills = rs.getInt("kills");
                int mobKills = rs.getInt("mob_kills");
                int deaths = rs.getInt("deaths");
                int blocksBroken = rs.getInt("blocks_broken");
                int blocksPlaced = rs.getInt("blocks_placed");
                int messagesSent = rs.getInt("messages_sent");
                String playtime = formatPlaytime(rs.getInt("playtime"));
                hud.updateStats(kills, mobKills, deaths, blocksBroken, blocksPlaced, messagesSent, playtime);
            }
        } catch (SQLException e) {
            plugin.getLogger().at(java.util.logging.Level.SEVERE).log("Error updating HUD stats: " + e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException ignored) {
                }
            }
        }
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
