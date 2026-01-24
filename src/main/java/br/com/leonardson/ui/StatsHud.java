package br.com.leonardson.ui;

import com.hypixel.hytale.server.core.entity.entities.player.hud.CustomUIHud;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;

public class StatsHud extends CustomUIHud {
    private int kills;
    private int mobKills;
    private int deaths;
    private int blocksBroken;
    private int blocksPlaced;
    private int messagesSent;
    private String playtime;
    private boolean visible;

    public StatsHud(@Nonnull PlayerRef playerRef) {
        super(playerRef);
        this.playtime = "0m";
        this.visible = true;
    }

    @Override
    protected void build(@Nonnull UICommandBuilder uiCommandBuilder) {
        uiCommandBuilder.append("StatsHud.ui");
        applyValues(uiCommandBuilder);
    }

    public void updateStats(
            int kills,
            int mobKills,
            int deaths,
            int blocksBroken,
            int blocksPlaced,
            int messagesSent,
            @Nonnull String playtime
    ) {
        this.kills = kills;
        this.mobKills = mobKills;
        this.deaths = deaths;
        this.blocksBroken = blocksBroken;
        this.blocksPlaced = blocksPlaced;
        this.messagesSent = messagesSent;
        this.playtime = playtime;

        UICommandBuilder commandBuilder = new UICommandBuilder();
        applyValues(commandBuilder);
        update(false, commandBuilder);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        UICommandBuilder commandBuilder = new UICommandBuilder();
        applyValues(commandBuilder);
        update(false, commandBuilder);
    }

    public boolean isVisible() {
        return visible;
    }

    private void applyValues(@Nonnull UICommandBuilder commandBuilder) {
        commandBuilder.set("#KillsValue.Text", String.valueOf(kills));
        commandBuilder.set("#MobKillsValue.Text", String.valueOf(mobKills));
        commandBuilder.set("#DeathsValue.Text", String.valueOf(deaths));
        commandBuilder.set("#BlocksBrokenValue.Text", String.valueOf(blocksBroken));
        commandBuilder.set("#BlocksPlacedValue.Text", String.valueOf(blocksPlaced));
        commandBuilder.set("#MessagesSentValue.Text", String.valueOf(messagesSent));
        commandBuilder.set("#PlaytimeValue.Text", playtime);
        commandBuilder.set("#StatsHudPanel.Visible", visible);
    }
}
