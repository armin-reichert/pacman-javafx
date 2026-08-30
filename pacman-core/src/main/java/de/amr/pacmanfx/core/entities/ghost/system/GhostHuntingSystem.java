/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;


import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.RoamingSystem;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.gameplay.hunt.GhostHuntingStrategy;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;

public class GhostHuntingSystem {

    private final GhostWorldMovementPolicy movementPolicy;
    private final RoamingSystem roamingSystem;

    public GhostHuntingSystem(GhostWorldMovementPolicy movementPolicy, RoamingSystem roamingSystem) {
        this.movementPolicy = movementPolicy;
        this.roamingSystem = roamingSystem;
    }

    public void update(GameContext game, GameLevel level, Ghost ghost, GhostHuntingStrategy huntingStrategy) {
        final ActorSpeedRules speedRules = game.variant().rules().actorSpeedRules();
        switch (ghost.state().enumValue()) {
            case HUNTING_PAC -> {
                final float speed = speedRules.ghostSpeed(game, ghost);
                huntingStrategy.hunt(level, ghost, speed, movementPolicy);
            }
            case FRIGHTENED -> {
                final float speed = speedRules.ghostSpeed(game, ghost);
                roamingSystem.roam(level, ghost, ghost.worldNavigation(), movementPolicy, speed);
            }
        }
    }
}
