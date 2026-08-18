/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.gameplay.hunt;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;

public interface GhostHuntingStrategy {

    void hunt(GameLevel level, Ghost ghost, MovementSystem motor, float speed, WorldMovementPolicy worldMovementPolicy);

    default Vector2i computeScatterTile(WorldMap worldMap, Ghost ghost) {
        return worldMap.terrainLayer().ghostScatterTile(ghost.personality());
    }
}
