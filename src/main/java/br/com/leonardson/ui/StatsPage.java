package br.com.leonardson.ui;

import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.BasicCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import javax.annotation.Nonnull;

public class StatsPage extends BasicCustomUIPage {
    private final String playerName;
    private final int kills;
    private final int mobKills;
    private final int deaths;
    private final int blocksPlaced;
    private final int blocksBroken;
    private final int itemsDropped;
    private final int itemsPickedUp;
    private final int messagesSent;
    private final String distanceTraveled;
    private final String playtime;

    public StatsPage(
            @Nonnull PlayerRef playerRef,
            @Nonnull String playerName,
            int kills,
            int mobKills,
            int deaths,
            int blocksPlaced,
            int blocksBroken,
            int itemsDropped,
            int itemsPickedUp,
            int messagesSent,
            @Nonnull String distanceTraveled,
            @Nonnull String playtime
    ) {
        super(playerRef, CustomPageLifetime.CanDismiss);
        this.playerName = playerName;
        this.kills = kills;
        this.mobKills = mobKills;
        this.deaths = deaths;
        this.blocksPlaced = blocksPlaced;
        this.blocksBroken = blocksBroken;
        this.itemsDropped = itemsDropped;
        this.itemsPickedUp = itemsPickedUp;
        this.messagesSent = messagesSent;
        this.distanceTraveled = distanceTraveled;
        this.playtime = playtime;
    }

    @Override
    public void build(UICommandBuilder commandBuilder) {
        commandBuilder.append("Stats.ui");
        commandBuilder.set("#Subtitle.Text", playerName);

        commandBuilder.set("#CombatKills.Text", "Player Kills: " + kills);
        commandBuilder.set("#CombatMobKills.Text", "Mob Kills: " + mobKills);
        commandBuilder.set("#CombatDeaths.Text", "Deaths: " + deaths);

        commandBuilder.set("#BuildingPlaced.Text", "Blocks Placed: " + blocksPlaced);
        commandBuilder.set("#BuildingBroken.Text", "Blocks Broken: " + blocksBroken);

        commandBuilder.set("#ItemsDropped.Text", "Items Dropped: " + itemsDropped);
        commandBuilder.set("#ItemsPickedUp.Text", "Items Picked Up: " + itemsPickedUp);

        commandBuilder.set("#SocialMessages.Text", "Messages Sent: " + messagesSent);

        commandBuilder.set("#GeneralDistance.Text", "Distance Traveled: " + distanceTraveled);
        commandBuilder.set("#GeneralPlaytime.Text", "Playtime: " + playtime);
    }
}
