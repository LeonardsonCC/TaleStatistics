package br.com.leonardson.listeners;

import br.com.leonardson.Main;
import br.com.leonardson.database.DatabaseManager;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.modules.entity.damage.event.KillFeedEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class PlayerKillEventSystem extends EntityEventSystem<EntityStore, KillFeedEvent.KillerMessage> {
    @Nonnull
    private final ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
    private final Main plugin;
    private final DatabaseManager database;

    public PlayerKillEventSystem(@Nonnull Main plugin, @Nonnull DatabaseManager database) {
        super(KillFeedEvent.KillerMessage.class);
        this.plugin = plugin;
        this.database = database;
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull KillFeedEvent.KillerMessage event
    ) {
        PlayerRef playerRef = archetypeChunk.getComponent(index, this.playerRefComponentType);
        if (playerRef == null) {
            return;
        }

        // Differentiate between player kills and mob kills
        PlayerRef targetPlayerRef = store.getComponent(event.getTargetRef(), this.playerRefComponentType);
        if (targetPlayerRef != null) {
            database.incrementStat(playerRef.getUuid().toString(), "kills", 1);
            plugin.getLogger().at(Level.FINE).log("Player kill recorded for " + playerRef.getUsername());
        } else {
            database.incrementStat(playerRef.getUuid().toString(), "mob_kills", 1);
            plugin.getLogger().at(Level.FINE).log("Mob kill recorded for " + playerRef.getUsername());
        }
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return this.playerRefComponentType;
    }
}
