/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.ElroyComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostHouseAccessComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.gameplay.hunt.GhostHuntingStrategy;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.rules.ActorSpeedRules;
import de.amr.pacmanfx.core.rules.GameRules;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class GhostStateSystem {

    // Ghosts in these states are updated other ghost(s) are eaten and hunting is frozen
    public static final Set<GhostState> UPDATED_GHOST_STATES_WHILE_EATEN = Set.of(
        GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE);

    public GhostStateSystem() {}

    public void update(GameContext game, GameLevel level, Ghost ghost) {
        requireNonNull(game);
        requireNonNull(ghost);

        final Pac pac = level.entities().pac();

        ghost.state().setStateTick(game.state().timer().tickCount());
        ghost.state().setFlashing(pac.power().isFading());
        ghost.state().setThreatenedByPac(isGhostThreatenedByPac(level, ghost, pac));

        final GameSystems systems = game.variant().systems();
        final ActorSpeedRules speedRules = game.variant().rules().actorSpeedRules();

        switch (ghost.state().enumValue()) {
            case LOCKED -> {
                // gets unlocked by gatekeeper for now
            }
            case ENTERING_HOUSE -> {
                if (ghost.houseAccess().reachedRevivalPosition()) {
                    setState(ghost, GhostState.LOCKED);
                }
            }
            case LEAVING_HOUSE -> {
                if (ghost.houseAccess().leftHouse()) {
                    final GhostState state = ghost.state().isThreatenedByPac()
                        ? GhostState.FRIGHTENED
                        : GhostState.HUNTING_PAC;
                    setState(ghost, state);
                }
            }
            case RETURNING_HOME -> {
                if (ghost.houseAccess().reachedHouseEntry()) {
                    setState(ghost, GhostState.ENTERING_HOUSE);
                }
            }
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
                systems.roaming().roam(level, ghost, ghost.worldNavigation(), movementPolicy, systems.motor(), speed);
            }
        }
    }

    public void setState(Ghost ghost, GhostState newState) {
        requireNonNull(ghost);
        requireNonNull(newState);

        ghost.state().setEnumValue(newState);
    }

    public void setElroyEnabled(Ghost ghost, boolean enabled) {
        requireNonNull(ghost);

        ghost.optComp(ElroyComp.class).ifPresent(elroy -> elroy.setEnabled(enabled));
    }

    public void updateElroyState(GameContext game) {
        final GameLevel level = game.session().level();
        final Ghost ghost = level.entities().ghost(GhostPersonality.RED_GHOST_SHADOW);
        final GameRules rules = game.variant().rules();
        ghost.optComp(ElroyComp.class).ifPresent(elroy -> {
            if (rules.ghostBecomesElroy1(level, ghost)) {
                elroy.setBoost(ElroyComp.Boost.MEDIUM);
            } else if (rules.ghostBecomesElroy2(level, ghost)) {
                elroy.setBoost(ElroyComp.Boost.LARGE);
            }
        });
    }

    private boolean isGhostThreatenedByPac(GameLevel level, Ghost ghost, Pac pac) {
        return pac.power().isActive() && !level.isInGhostKilledChain(ghost);
    }
}
