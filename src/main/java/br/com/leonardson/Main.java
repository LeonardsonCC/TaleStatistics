package br.com.leonardson;

import javax.annotation.Nonnull;

import br.com.leonardson.database.DatabaseManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class Main extends JavaPlugin {
    private static Main instance;
    private DatabaseManager databaseManager;

    public Main(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
    }

    @Override
    protected void setup() {
        super.setup();

        // Initialize database
        databaseManager = new DatabaseManager(this.getLogger());
        databaseManager.connect();

        // this.getCommandRegistry().registerCommand(new ShopCommand());
    }

    public static Main getInstance() {
        return instance;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
