/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;


import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.gameplay.hunt.GhostHuntingStrategy;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;

public class GhostHuntingSystem {

    public void update(GameContext game, GameLevel level, Ghost ghost) {

        final GameSystems systems = game.variant().systems();
        final ActorSpeedRules speedRules = game.variant().rules().actorSpeedRules();

        switch (ghost.state().enumValue()) {
            case HUNTING_PAC -> {
                //TODO This does not belong here!
                final GhostHuntingStrategy huntingStrategy = systems.ghostHuntingStrategy(ghost.personality());
                final GhostWorldMovementPolicy movementPolicy = systems.ghostWorldMovementPolicy();
                final float speed = speedRules.ghostSpeed(game, ghost);
                huntingStrategy.hunt(level, ghost, systems.motor(), speed, movementPolicy);
            }

            case FRIGHTENED -> {
                //TODO This does not belong here!
                final GhostWorldMovementPolicy movementPolicy = systems.ghostWorldMovementPolicy();
                final float speed = speedRules.ghostSpeed(game, ghost);
                systems.roaming().roam(level, ghost, ghost.worldNavigation(), movementPolicy, speed);
            }

        }
    }
}
