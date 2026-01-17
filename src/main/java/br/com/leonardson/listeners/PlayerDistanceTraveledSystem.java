package br.com.leonardson.listeners;

import br.com.leonardson.Main;
import br.com.leonardson.database.DatabaseManager;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class PlayerDistanceTraveledSystem extends EntityTickingSystem<EntityStore> {
    private static final double FLUSH_DISTANCE_THRESHOLD = 1.0; // blocks
    private static final long FLUSH_TIME_THRESHOLD_MS = 1000L; // 1 second

    @Nonnull
    private final ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
    @Nonnull
    private final ComponentType<EntityStore, TransformComponent> transformComponentType = TransformComponent.getComponentType();

    private final Main plugin;
    private final DatabaseManager database;

    private final Map<UUID, Vector3d> lastPositions = new HashMap<>();
    private final Map<UUID, Double> pendingDistances = new HashMap<>();
    private final Map<UUID, Long> lastFlushTimes = new HashMap<>();

    public PlayerDistanceTraveledSystem(@Nonnull Main plugin, @Nonnull DatabaseManager database) {
        this.plugin = plugin;
        this.database = database;
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
        PlayerRef playerRef = archetypeChunk.getComponent(index, this.playerRefComponentType);
        TransformComponent transformComponent = archetypeChunk.getComponent(index, this.transformComponentType);

        if (playerRef == null || transformComponent == null) {
            return;
        }

        UUID uuid = playerRef.getUuid();
        Vector3d currentPosition = transformComponent.getPosition();
        Vector3d lastPosition = lastPositions.get(uuid);

        if (lastPosition == null) {
            lastPositions.put(uuid, new Vector3d(currentPosition));
            lastFlushTimes.put(uuid, System.currentTimeMillis());
            return;
        }

        double distance = lastPosition.distanceTo(currentPosition);
        if (distance <= 0.0 || Double.isNaN(distance)) {
            return;
        }

        lastPosition.assign(currentPosition);
        pendingDistances.merge(uuid, distance, Double::sum);

        long now = System.currentTimeMillis();
        long lastFlush = lastFlushTimes.getOrDefault(uuid, now);
        double pending = pendingDistances.getOrDefault(uuid, 0.0);

        if (pending >= FLUSH_DISTANCE_THRESHOLD || now - lastFlush >= FLUSH_TIME_THRESHOLD_MS) {
            flushPending(uuid, now);
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return Archetype.of(this.playerRefComponentType, this.transformComponentType);
    }

    public void onPlayerDisconnect(@Nonnull UUID uuid) {
        flushPending(uuid, System.currentTimeMillis());
        lastPositions.remove(uuid);
        pendingDistances.remove(uuid);
        lastFlushTimes.remove(uuid);
    }

    public void flushAll() {
        long now = System.currentTimeMillis();
        for (UUID uuid : pendingDistances.keySet()) {
            flushPending(uuid, now);
        }
    }

    private void flushPending(@Nonnull UUID uuid, long now) {
        double pending = pendingDistances.getOrDefault(uuid, 0.0);
        if (pending <= 0.0) {
            lastFlushTimes.put(uuid, now);
            return;
        }

        database.incrementStat(uuid.toString(), "distance_traveled", pending);
        pendingDistances.put(uuid, 0.0);
        lastFlushTimes.put(uuid, now);
        plugin.getLogger().at(Level.FINE).log("Distance traveled updated for player: " + uuid);
    }
}