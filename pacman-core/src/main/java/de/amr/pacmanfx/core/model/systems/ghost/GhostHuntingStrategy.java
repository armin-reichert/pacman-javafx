/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.world.WorldMap;

public interface GhostHuntingStrategy {

    void hunt(GameContext gameContext, Ghost ghost, float speed);

    default Vector2i computeScatterTile(WorldMap worldMap, Ghost ghost) {
        return worldMap.terrainLayer().ghostScatterTile(ghost.personality());
    }
}
