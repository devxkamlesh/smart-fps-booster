# Changelog

All notable changes to Smart FPS Booster are documented here.
This project follows [Semantic Versioning](https://semver.org/).

## [2.0.0] - 2026-07-01

Major release: full update to Minecraft 26.1 & 26.2 and a redesigned interface.

### Added
- **Edge Alert toggle** — the red screen-edge flash shown on FPS/GPU spikes can now be turned off in **Settings → Visuals & Alerts → Edge Alert**.
- **Redesigned UI** built on the new 26.1 render-state model:
  - Modern dashboard with gradient panels, drop shadows, accent title bars and stat cards (FPS / Average / 1% Low).
  - Themed memory and session-history bars, improved live FPS chart.
  - Rebuilt, grouped settings screen with a smooth scrollbar and section headers.
  - Polished setup wizard (with step indicators), overlay-position screen and notifications.
  - Refreshed FPS overlay styles (gradient backgrounds, accent bars).

### Changed
- **Now targets Minecraft 26.1 & 26.2** (single build, loads on both).
- Migrated the entire codebase from Yarn to **Mojang official mappings** (26.1 is the first unobfuscated Minecraft release).
- Updated toolchain: Fabric Loom 1.17, Gradle 9.5.1, **Java 25**, Fabric Loader 0.19.3, Fabric API 0.153.0.
- HUD rendering migrated from the removed `HudRenderCallback` to `HudElementRegistry`.
- Keybindings migrated to the new `KeyMappingHelper` / `KeyMapping.Category` API.

### Requirements
- Minecraft 26.1 or 26.2 (Fabric)
- Java 25+
- Fabric API

## [1.0.0]

- Initial release: smart profiles, live FPS dashboard, dynamic optimization,
  setting locks, lag spike alert, benchmark, and mod detection (Sodium / Lithium / Iris).
- Supported Minecraft 1.20.x – 1.21.x (Fabric).
