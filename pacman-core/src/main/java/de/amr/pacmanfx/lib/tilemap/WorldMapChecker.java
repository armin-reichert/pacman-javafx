package de.amr.pacmanfx.lib.tilemap;

import de.amr.pacmanfx.lib.StopWatch;
import de.amr.pacmanfx.lib.Vector2i;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.List;

public interface WorldMapChecker {

    record WorldMapCheckResult(List<Vector2i> tilesWithErrors) {}

    static WorldMapCheckResult check(WorldMap worldMap) {
        StopWatch stopWatch = new StopWatch();
        var tilesWithErrors = worldMap.buildObstacleList();

        Duration passedTime = stopWatch.passedTime();
        Logger.info("World map check took {} milliseconds", passedTime.toMillis());
        return new WorldMapCheckResult(tilesWithErrors);
    }
}
