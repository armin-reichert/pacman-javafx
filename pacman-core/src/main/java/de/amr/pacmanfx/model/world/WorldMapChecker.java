package de.amr.pacmanfx.model.world;

import de.amr.pacmanfx.lib.math.Vector2i;

import java.util.List;

public interface WorldMapChecker {

    record WorldMapCheckResult(List<Vector2i> tilesWithErrors) {}

    static WorldMapCheckResult check(WorldMap worldMap) {
        var tilesWithErrors = worldMap.terrainLayer().buildObstacleList();
        return new WorldMapCheckResult(tilesWithErrors);
    }
}
