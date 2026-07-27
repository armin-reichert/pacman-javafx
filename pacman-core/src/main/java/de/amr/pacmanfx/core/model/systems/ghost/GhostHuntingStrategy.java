package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.level.GameLevel;

public interface GhostHuntingStrategy {

    void hunt(GameContext gameContext, Ghost ghost, float speed);

    default Vector2i computeScatterTile(GameContext gameContext, Ghost ghost) {
        final GameLevel level = gameContext.assertLevel();
        return level.worldMap().terrainLayer().ghostScatterTile(ghost.personality());
    }
}
