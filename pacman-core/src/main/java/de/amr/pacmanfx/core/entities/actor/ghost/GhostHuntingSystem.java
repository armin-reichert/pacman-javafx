/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.actor.ghost;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.world.WorldRoamingSystem;
import de.amr.pacmanfx.core.gameplay.hunt.GhostHuntingStrategy;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;

public class GhostHuntingSystem {

    private final GhostWorldMovementPolicy movementPolicy;
    private final WorldRoamingSystem roamingSystem;

    public GhostHuntingSystem(GhostWorldMovementPolicy movementPolicy, WorldRoamingSystem roamingSystem) {
        this.movementPolicy = movementPolicy;
        this.roamingSystem = roamingSystem;
    }

    public void update(GameContext game, GameLevel level, Ghost ghost, GhostHuntingStrategy huntingStrategy) {
        final ActorSpeedRules speedRules = game.playConfig().rules().actorSpeedRules();
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
