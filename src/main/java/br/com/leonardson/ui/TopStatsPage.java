package br.com.leonardson.ui;

import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.server.core.entity.entities.player.pages.BasicCustomUIPage;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import javax.annotation.Nonnull;
import java.util.List;

public class TopStatsPage extends BasicCustomUIPage {
    private final String statTitle;
    private final List<String> entries;

    public TopStatsPage(@Nonnull PlayerRef playerRef, @Nonnull String statTitle, @Nonnull List<String> entries) {
        super(playerRef, CustomPageLifetime.CanDismiss);
        this.statTitle = statTitle;
        this.entries = entries;
    }

    @Override
    public void build(UICommandBuilder commandBuilder) {
        commandBuilder.append("TopStats.ui");
        commandBuilder.set("#Subtitle.Text", statTitle);

        for (int i = 0; i < 10; i++) {
            String text = i < entries.size() ? entries.get(i) : "";
            commandBuilder.set("#Entry" + (i + 1) + ".Text", text);
        }
    }
}
