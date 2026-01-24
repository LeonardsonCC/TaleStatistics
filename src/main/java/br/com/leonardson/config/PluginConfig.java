package br.com.leonardson.config;

import com.hypixel.hytale.logger.HytaleLogger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;

public class PluginConfig {
    public static final String FILE_NAME = "config.json";
    private static final String KEY_DEFAULT_SIDEBAR_ENABLED = "default_sidebar_enabled";
    private static final Pattern DEFAULT_SIDEBAR_PATTERN = Pattern.compile(
            "\"" + KEY_DEFAULT_SIDEBAR_ENABLED + "\"\\s*:\\s*(true|false)",
            Pattern.CASE_INSENSITIVE
    );

    private final Path configPath;
    private final HytaleLogger logger;
    private boolean defaultSidebarEnabled = true;

    public PluginConfig(Path dataDirectory, HytaleLogger logger) {
        this.configPath = dataDirectory.resolve(FILE_NAME);
        this.logger = logger;
    }

    public void load() {
        try {
            Files.createDirectories(configPath.getParent());
        } catch (IOException e) {
            logger.at(Level.SEVERE).withCause(e).log("Failed to create config directory: %s", configPath.getParent());
            return;
        }

        if (!Files.exists(configPath)) {
            saveDefaults();
            return;
        }

        String raw;
        try {
            raw = Files.readString(configPath);
        } catch (IOException e) {
            logger.at(Level.SEVERE).withCause(e).log("Failed to read config file: %s", configPath);
            return;
        }

        Matcher matcher = DEFAULT_SIDEBAR_PATTERN.matcher(raw);
        if (matcher.find()) {
            defaultSidebarEnabled = Boolean.parseBoolean(matcher.group(1));
        }
    }

    public boolean isDefaultSidebarEnabled() {
        return defaultSidebarEnabled;
    }

    private void saveDefaults() {
        try {
            try (var resourceStream = PluginConfig.class.getClassLoader().getResourceAsStream(FILE_NAME)) {
                if (resourceStream != null) {
                    Files.copy(resourceStream, configPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            }

            String content = "{\n  \"" + KEY_DEFAULT_SIDEBAR_ENABLED + "\": "
                    + defaultSidebarEnabled + "\n}" + System.lineSeparator();
            Files.writeString(
                configPath,
                content,
                StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (IOException e) {
            logger.at(Level.SEVERE).withCause(e).log("Failed to create config file: %s", configPath);
        }
    }
}
