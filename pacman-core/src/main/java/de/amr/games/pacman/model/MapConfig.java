/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model;

import de.amr.games.pacman.lib.tilemap.WorldMap;

import java.util.Map;

public record MapConfig(int mapNumber, WorldMap worldMap, Map<String, String> colorScheme) {
}
