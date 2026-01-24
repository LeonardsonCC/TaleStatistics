package br.com.leonardson.commands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import br.com.leonardson.Main;
import br.com.leonardson.ui.StatsHudSystem;

public class StatsSidebarCommand extends AbstractPlayerCommand {
    private final DefaultArg<String> action;

    public StatsSidebarCommand() {
        super("sidebar", "Toggle the stats sidebar", false);

        this.action = this.withDefaultArg(
                "action",
                "on, off, or toggle",
                ArgTypes.STRING,
                "toggle",
                "Desc of Default: toggle"
        );
    }
    
    @Override
    protected void execute(CommandContext context, Store<EntityStore> store, Ref<EntityStore> ref, PlayerRef playerRef,
            World world) {

        Player playerComponent = store.getComponent(ref, Player.getComponentType());
        if (playerComponent == null) {
            playerRef.sendMessage(Message.raw("Unable to update the stats sidebar right now."));
            return;
        }

        StatsHudSystem hudSystem = Main.getInstance().getStatsHudSystem();
        if (hudSystem == null) {
            playerRef.sendMessage(Message.raw("Stats sidebar system is not available."));
            return;
        }

        String rawValue = action.get(context).trim();
        boolean enabled;
        if (rawValue.equalsIgnoreCase("on") || rawValue.equalsIgnoreCase("show") || rawValue.equalsIgnoreCase("enable")) {
            enabled = true;
        } else if (rawValue.equalsIgnoreCase("off") || rawValue.equalsIgnoreCase("hide") || rawValue.equalsIgnoreCase("disable")) {
            enabled = false;
        } else if (rawValue.equalsIgnoreCase("toggle")) {
            enabled = !hudSystem.isHudEnabled(playerRef.getUuid());
        } else {
            playerRef.sendMessage(Message.raw("Usage: /stats sidebar on|off"));
            return;
        }

        hudSystem.setHudEnabled(playerRef, playerComponent, enabled);
        playerRef.sendMessage(Message.raw(enabled ? "Stats sidebar enabled." : "Stats sidebar disabled."));
    }
}
