# TaleStatistics
Tale Statistics mod

Server-side Hytale plugin that tracks player activity and exposes stats in UI pages plus an optional HUD sidebar. It stores stats in a local SQLite database and provides player commands for stats, the sidebar, and top rankings.

<img width="1266" height="712" alt="image" src="https://github.com/user-attachments/assets/179e1b2a-52c9-4f7e-a743-2138dbb82f13" />

## Features
- Tracks kills, mob kills, deaths, blocks placed/broken/damaged/used
- Tracks items dropped/picked up/crafted
- Tracks chat messages sent
- Tracks distance traveled and playtime
- Stats UI pages for personal and top rankings
- Optional stats HUD sidebar (enabled by default, toggle with command)

## Commands
- `/stats show` — View your own stats UI.
- `/stats show <player>` — View another player’s stats (by name).
- `/stats sidebar [on|off|toggle]` — Toggle or set the stats sidebar (default: toggle).
- `/stats top <stat>` — View top 10 players for a stat.

Valid stats for `/topstats`:
- `kills`, `mob_kills`, `deaths`
- `blocks_placed`, `blocks_broken`, `blocks_damaged`, `blocks_used`
- `items_dropped`, `items_picked_up`, `items_crafted`
- `messages_sent`, `distance_traveled`, `playtime`

## Permissions

Default permissions:

| Command | Permission |
|---------|------------|
| `/stats show` | `leonardson.talestatistics.command.stats.show` |
| `/stats sidebar [on|off|toggle]` | `leonardson.talestatistics.command.stats.sidebar` |
| `/stats top <stat>` | `leonardson.talestatistics.command.stats.top` |

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
