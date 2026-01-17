package br.com.leonardson.database;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Constants;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class DatabaseManager {
    private static final String MAIN_PATH = Constants.UNIVERSE_PATH.resolve("HytaleStatistics").toAbsolutePath().toString();
    private static final String DATABASE_PATH = MAIN_PATH + File.separator + "player_stats.db";
    
    private Connection connection;
    private final HytaleLogger logger;

    public DatabaseManager(HytaleLogger logger) {
        this.logger = logger;
    }

    /**
     * Ensures the main directory exists
     */
    private void ensureMainDirectory() {
        File directory = new File(MAIN_PATH);
        if (!directory.exists()) {
            directory.mkdirs();
            logger.at(Level.INFO).log("Created plugin directory at: " + MAIN_PATH);
        }
    }

    /**
     * Ensures the database file is created
     */
    private void ensureDatabaseFile() {
        File databaseFile = new File(DATABASE_PATH);
        if (!databaseFile.exists()) {
            try {
                databaseFile.createNewFile();
                logger.at(Level.INFO).log("Created database file at: " + DATABASE_PATH);
            } catch (Exception e) {
                logger.at(Level.SEVERE).log("Failed to create database file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Establishes a connection to the SQLite database
     */
    public void connect() {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            logger.at(Level.INFO).log("SQLite JDBC driver loaded successfully");
            
            // Ensure main directory exists
            ensureMainDirectory();
            
            // Ensure database file exists
            ensureDatabaseFile();

            // Establish connection
            String url = "jdbc:sqlite:" + DATABASE_PATH;
            connection = DriverManager.getConnection(url);
            logger.at(Level.INFO).log("Successfully connected to SQLite database at: " + DATABASE_PATH);

            // Initialize tables
            initializeTables();
            
            // Verify connection by running a simple query
            if (connection != null && !connection.isClosed()) {
                logger.at(Level.INFO).log("Database connection verified and ready");
            }
        } catch (ClassNotFoundException e) {
            logger.at(Level.SEVERE).log("SQLite JDBC driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            logger.at(Level.SEVERE).log("Failed to connect to database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates the necessary tables if they don't exist
     */
    private void initializeTables() {
        String createPlayerStatsTable = """
            CREATE TABLE IF NOT EXISTS player_stats (
                player_uuid TEXT PRIMARY KEY,
                player_name TEXT NOT NULL,
                kills INTEGER DEFAULT 0,
                mob_kills INTEGER DEFAULT 0,
                deaths INTEGER DEFAULT 0,
                blocks_broken INTEGER DEFAULT 0,
                blocks_placed INTEGER DEFAULT 0,
                blocks_damaged INTEGER DEFAULT 0,
                blocks_used INTEGER DEFAULT 0,
                items_dropped INTEGER DEFAULT 0,
                items_picked_up INTEGER DEFAULT 0,
                items_crafted INTEGER DEFAULT 0,
                messages_sent INTEGER DEFAULT 0,
                distance_traveled REAL DEFAULT 0.0,
                playtime INTEGER DEFAULT 0,
                last_seen INTEGER NOT NULL,
                first_joined INTEGER NOT NULL
            )
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createPlayerStatsTable);
            ensureColumnExists("player_stats", "mob_kills", "INTEGER DEFAULT 0");
            logger.at(Level.INFO).log("Database tables initialized successfully");
        } catch (SQLException e) {
            logger.at(Level.SEVERE).log("Failed to initialize database tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ensures a column exists in a table
     */
    private void ensureColumnExists(String tableName, String columnName, String columnDefinition) {
        try (Statement stmt = connection.createStatement()) {
            var rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")");
            boolean exists = false;
            while (rs.next()) {
                if (columnName.equalsIgnoreCase(rs.getString("name"))) {
                    exists = true;
                    break;
                }
            }
            rs.close();

            if (!exists) {
                stmt.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnDefinition);
                logger.at(Level.INFO).log("Added missing column: " + columnName);
            }
        } catch (SQLException e) {
            logger.at(Level.SEVERE).log("Failed to ensure column " + columnName + ": " + e.getMessage());
        }
    }

    /**
     * Gets the current database connection
     */
    public Connection getConnection() {
        try {
            // Check if connection is still valid
            if (connection == null || connection.isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            logger.at(Level.SEVERE).log("Failed to check connection status: " + e.getMessage());
        }
        return connection;
    }

    /**
     * Closes the database connection
     */
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logger.at(Level.INFO).log("Database connection closed");
            }
        } catch (SQLException e) {
            logger.at(Level.SEVERE).log("Error while closing database connection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Checks if the database connection is active
     */
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Initializes a player in the database if they don't exist
     */
    public void initializePlayer(String uuid, String name) {
        String sql = "INSERT OR IGNORE INTO player_stats (player_uuid, player_name, first_joined, last_seen) VALUES (?, ?, ?, ?)";
        long currentTime = System.currentTimeMillis();
        
        try (var pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, uuid);
            pstmt.setString(2, name);
            pstmt.setLong(3, currentTime);
            pstmt.setLong(4, currentTime);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.at(Level.SEVERE).log("Failed to initialize player: " + e.getMessage());
        }
    }

    /**
     * Updates last seen timestamp for a player
     */
    public void updateLastSeen(String uuid) {
        String sql = "UPDATE player_stats SET last_seen = ? WHERE player_uuid = ?";
        
        try (var pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.setString(2, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.at(Level.SEVERE).log("Failed to update last seen: " + e.getMessage());
        }
    }

    /**
     * Increments a stat by a certain amount
     */
    public void incrementStat(String uuid, String statName, int amount) {
        String sql = "UPDATE player_stats SET " + statName + " = " + statName + " + ? WHERE player_uuid = ?";
        
        try (var pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, amount);
            pstmt.setString(2, uuid);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logger.at(Level.SEVERE).log("Failed to increment stat " + statName + ": " + e.getMessage());
        }
    }

    /**
     * Increments a stat by 1
     */
    public void incrementStat(String uuid, String statName) {
        incrementStat(uuid, statName, 1);
    }

    /**
     * Gets a player's statistics
     */
    public java.sql.ResultSet getPlayerStats(String uuid) {
        String sql = "SELECT * FROM player_stats WHERE player_uuid = ?";
        
        try {
            var pstmt = getConnection().prepareStatement(sql);
            pstmt.setString(1, uuid);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            logger.at(Level.SEVERE).log("Failed to get player stats: " + e.getMessage());
            return null;
        }
    }
}
