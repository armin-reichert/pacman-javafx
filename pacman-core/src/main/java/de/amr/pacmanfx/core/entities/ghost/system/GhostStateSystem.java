/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.entities.ghost.comp.ElroyComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GhostPersonality;
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

        switch (ghost.state().enumValue()) {
            case LOCKED -> {
                if (ghost.houseAccess().isUnlockRequested()) {
                    final boolean insideHouse = ghost.worldInfo().house().contains(ghost.pos().tile());
                    if (insideHouse) {
                        setState(ghost, GhostState.LEAVING_HOUSE);
                    }
                    else {
                        setState(ghost, ghost.state().isThreatenedByPac() ? GhostState.FRIGHTENED : GhostState.HUNTING_PAC);
                    }
                    ghost.houseAccess().setUnlockRequested(false);
                }
            }
            case ENTERING_HOUSE -> {
                if (ghost.houseAccess().reachedRevivalPosition()) {
                    ghost.state().setKilled(false);
                    setState(ghost, GhostState.LOCKED);
                }
            }
            case LEAVING_HOUSE -> {
                if (ghost.houseAccess().leftHouse()) {
                    setState(ghost, ghost.state().isThreatenedByPac() ? GhostState.FRIGHTENED : GhostState.HUNTING_PAC);
                }
            }
            case RETURNING_HOME -> {
                if (ghost.houseAccess().reachedHouseEntry()) {
                    setState(ghost, GhostState.ENTERING_HOUSE);
                }
            }
        }
    }

    public void startHuntingPac(Ghost ghost) {
        ghost.state().setEnumValue(GhostState.HUNTING_PAC);
    }

    public void setVulnerable(Ghost ghost) {
        ghost.state().setEnumValue(GhostState.FRIGHTENED);
    }

    public void setOutOfDanger(Ghost ghost) {
        if (ghost.state().enumValue() == GhostState.FRIGHTENED) {
            ghost.state().setEnumValue(GhostState.HUNTING_PAC);
        }
    }

    public void setKilled(Ghost ghost) {
        ghost.state().setEnumValue(GhostState.EATEN); // changes to "returning home" after short time!
        ghost.state().setKilled(true); // remains true until ghost is revived inside house!
    }

    public void returnHome(Ghost ghost) {
        if (ghost.state().enumValue() == GhostState.EATEN) {
            ghost.state().setEnumValue(GhostState.RETURNING_HOME);
        }
    }

    private void setState(Ghost ghost, GhostState newState) {
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
