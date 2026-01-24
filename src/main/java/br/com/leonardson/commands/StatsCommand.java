package br.com.leonardson.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class StatsCommand extends AbstractCommandCollection {
    public StatsCommand() {
        super("stats", "Manage player statistics");
        addSubCommand(new StatsShowCommand());
        addSubCommand(new StatsSidebarCommand());
    }
}
