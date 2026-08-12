/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.comp.WorldNavigationComp;
import de.amr.pacmanfx.core.ecs.systems.RandomWorldMovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldMovementPolicy;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostStateComp;
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

        final Pac pac = level.entities().pac();
        final GhostStateComp state = ghost.requireComp(GhostStateComp.class);

        state.setFlashing(pac.power().isFading());
        state.setThreatenedByPac(isGhostThreatenedByPac(level, ghost, pac));

        final float speed = game.model().rules().actorSpeedRules().ghostSpeed(game, ghost);

        switch (ghost.ghostStateEnum()) {
            case LOCKED -> houseAccessSystem.stayInHouse(game, ghost, speed);

            case LEAVING_HOUSE -> {
                boolean leftHouse = houseAccessSystem.leaveHouse(game, ghost, speed);
                if (leftHouse) {
                    final GhostState newState = ghost.state().isThreatenedByPac() ? GhostState.FRIGHTENED : GhostState.HUNTING_PAC;
                    changeState(ghost, newState);
                }
            }

            case HUNTING_PAC -> {
                final GhostHuntingStrategy huntingStrategy = game.systems().ghostHuntingStrategy(ghost.personality());
                final WorldMovementPolicy worldMovementPolicy = game.systems().ghostWorldMovementPolicy();
                huntingStrategy.hunt(level, ghost, speed, worldMovementPolicy);
            }

            case FRIGHTENED -> {
                final RandomWorldMovementSystem roamingSystem = game.systems().roamingNavigator();
                WorldMovementPolicy worldMovementPolicy =  game.systems().ghostWorldMovementPolicy();
                WorldNavigationComp navigation = ghost.worldNavigation();
                roamingSystem.roam(navigation, worldMovementPolicy, level, ghost, speed);
            }

            case RETURNING_HOME -> houseAccessSystem.reachHouse(game, ghost, speed);

            case ENTERING_HOUSE -> houseAccessSystem.enterHouse(game, ghost, speed);

            case EATEN -> {}
        }
    }

    public void changeState(Ghost ghost, GhostState newState) {
        requireNonNull(ghost);
        requireNonNull(newState);
        ghost.requireComp(GhostStateComp.class).setGhostStateEnum(newState);
    }
    
    private boolean isGhostThreatenedByPac(GameLevel level, Ghost ghost, Pac pac) {
        return pac.power().isActive() && !level.isInGhostKilledChain(ghost);
    }
}
