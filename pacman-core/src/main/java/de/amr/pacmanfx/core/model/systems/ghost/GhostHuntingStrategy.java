package de.amr.pacmanfx.core.model.systems.ghost;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.actors.Ghost;

public interface GhostHuntingStrategy {

    void hunt(GameContext gameContext, Ghost ghost, float speed);
}
