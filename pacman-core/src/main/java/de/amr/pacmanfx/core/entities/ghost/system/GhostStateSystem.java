/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.*;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.gameplay.hunt.GhostHuntingStrategy;
import de.amr.pacmanfx.core.level.GameLevel;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class GhostStateSystem {

    // Ghosts in these states are updated other ghost(s) are eaten and hunting is frozen
    public static final Set<GhostState> UPDATED_GHOST_STATES_WHILE_EATEN = Set.of(
        GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE);

    private final GhostHouseAccessSystem houseAccessSystem;

    public GhostStateSystem(GhostHouseAccessSystem houseAccessSystem) {
        this.houseAccessSystem = requireNonNull(houseAccessSystem);
    }

    public void update(GameContext game, GameLevel level, Ghost ghost) {
        requireNonNull(game);
        requireNonNull(ghost);

        final GameSystems systems = game.variant().systems();

        final MovementSystem motor = systems.motor();
        final WorldNavigationSystem navigator = systems.worldNavigator();
        final WorldMovementPolicy movementPolicy = systems.ghostWorldMovementPolicy();
        final GhostHuntingStrategy huntingStrategy = systems.ghostHuntingStrategy(ghost.personality());
        final RoamingSystem roamingSystem = systems.roaming();

        final float speed = game.variant().rules().actorSpeedRules().ghostSpeed(game, ghost);

        final Pac pac = level.entities().pac();

        ghost.state().setFlashing(pac.power().isFading());
        ghost.state().setThreatenedByPac(isGhostThreatenedByPac(level, ghost, pac));

        switch (ghost.ghostStateEnum()) {

            case LOCKED -> houseAccessSystem.stayInHouse(ghost, navigator, motor, speed);

            case LEAVING_HOUSE -> {
                boolean leftHouse = houseAccessSystem.leaveHouse(ghost, navigator, motor, speed);
                if (leftHouse) {
                    final GhostState stateAfterLeavingHouse = ghost.state().isThreatenedByPac()
                        ? GhostState.FRIGHTENED
                        : GhostState.HUNTING_PAC;
                    changeGhostState(ghost, stateAfterLeavingHouse);
                }
            }

            case HUNTING_PAC -> huntingStrategy.hunt(level, ghost, motor, speed, movementPolicy);

            case FRIGHTENED ->
                roamingSystem.roam(level, ghost, ghost.worldNavigation(), movementPolicy, motor, speed);

            case RETURNING_HOME -> houseAccessSystem.reachHouse(level, ghost, navigator, movementPolicy, motor, speed)
                .ifPresent(newState -> changeGhostState(ghost, newState));

            case ENTERING_HOUSE -> houseAccessSystem.enterHouse(ghost, navigator, motor, speed)
                .ifPresent(newState -> changeGhostState(ghost, newState));

            case EATEN -> {}
        }
    }

    public void changeGhostState(Ghost ghost, GhostState newState) {
        requireNonNull(ghost);
        requireNonNull(newState);

        ghost.state().setGhostStateEnum(newState);
    }
    
    private boolean isGhostThreatenedByPac(GameLevel level, Ghost ghost, Pac pac) {
        return pac.power().isActive() && !level.isInGhostKilledChain(ghost);
    }
}
