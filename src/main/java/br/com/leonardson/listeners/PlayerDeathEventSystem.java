package br.com.leonardson.listeners;

import br.com.leonardson.Main;
import br.com.leonardson.database.DatabaseManager;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.server.core.modules.entity.damage.event.KillFeedEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PlayerDeathEventSystem extends EntityEventSystem<EntityStore, KillFeedEvent.DecedentMessage> {
    @Nonnull
    private final ComponentType<EntityStore, PlayerRef> playerRefComponentType = PlayerRef.getComponentType();
    private final Main plugin;
    private final DatabaseManager database;

    public PlayerDeathEventSystem(@Nonnull Main plugin, @Nonnull DatabaseManager database) {
        super(KillFeedEvent.DecedentMessage.class);
        this.plugin = plugin;
        this.database = database;
    }

    @Override
    public void handle(
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull KillFeedEvent.DecedentMessage event
    ) {
        PlayerRef playerRef = archetypeChunk.getComponent(index, this.playerRefComponentType);
        if (playerRef == null) {
            return;
        }

        database.incrementStat(playerRef.getUuid().toString(), "deaths", 1);
        plugin.getLogger().at(java.util.logging.Level.INFO).log("Death recorded for " + playerRef.getUsername());
    }

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return this.playerRefComponentType;
    }
}
