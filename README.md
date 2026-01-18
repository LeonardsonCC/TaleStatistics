# TaleStatistics
Tale Statistics mod

Server-side Hytale plugin that tracks player activity and exposes stats in UI pages. It stores stats in a local SQLite database and provides two player commands for viewing personal stats and top rankings.

## Features
- Tracks kills, mob kills, deaths, blocks placed/broken/damaged/used
- Tracks items dropped/picked up/crafted
- Tracks chat messages sent
- Tracks distance traveled and playtime
- Stats UI pages for personal and top rankings

## Commands
- `/stats` — View your own stats UI.
- `/stats <player>` — View another player’s stats (by name).
- `/topstats <stat>` — View top 10 players for a stat.

Valid stats for `/topstats`:
- `kills`, `mob_kills`, `deaths`
- `blocks_placed`, `blocks_broken`, `blocks_damaged`, `blocks_used`
- `items_dropped`, `items_picked_up`, `items_crafted`
- `messages_sent`, `distance_traveled`, `playtime`

Aliases are accepted (e.g., `kill`, `blocksplaced`, `distancetraveled`).

## Prerequisites
- Java 21
- HytaleServer.jar available at `libs/HytaleServer.jar` (compile-only)
- A Hytale server install on your Desktop at `HytaleServer` with:
  - `HytaleServer/Server/HytaleServer.jar`
  - `HytaleServer/Assets.zip`

## Build
From the project root:

- Windows (PowerShell):
  - `./gradlew clean fatJar`

Build output:
- `build/libs/TaleStatistics-1.0-SNAPSHOT-all.jar`

## Run (recommended)
Use the included script to build and launch the server with the plugin:

- `./run-server.ps1`

This will:
1. Build the fat JAR
2. Copy it into `Desktop/HytaleServer/mods`
3. Start the server with assets

## Manual install
1. Build the fat JAR.
2. Copy `build/libs/TaleStatistics-1.0-SNAPSHOT-all.jar` to your server `mods` folder.
3. Start the Hytale server normally.

## Data storage
The SQLite database is stored under the Hytale universe path in:
- `TaleStatistics/player_stats.db`

## Troubleshooting
- If the server doesn’t start, confirm `HytaleServer/Server/HytaleServer.jar` and `HytaleServer/Assets.zip` exist.
- If commands show no data, join the server and perform actions to generate stats.
