/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.entities.ghost.system;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.ghost.comp.ElroyComp;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.rules.GameRules;
import org.tinylog.Logger;

import java.util.Set;

import static java.util.Objects.requireNonNull;

public class GhostStateSystem {

    // Ghosts in these states are updated other ghost(s) are eaten and hunting is frozen
    public static final Set<GhostState> UPDATED_GHOST_STATES_WHILE_EATEN = Set.of(
        GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE);

    public GhostStateSystem() {}

    public void update(GameContext game, Ghost ghost) {
        requireNonNull(game);
        requireNonNull(ghost);

        ghost.state().setStateTick(game.state().timer().tickCount());

        final boolean pacHasPower = ghost.state().hasPacPower();
        final boolean alreadyKilled = ghost.state().killChainIndex() != -1;

        switch (ghost.state().enumValue()) {
            case LOCKED -> {
                if (ghost.houseAccess().isUnlockRequested()) {
                    final boolean insideHouse = ghost.worldInfo().house().contains(ghost.pos().tile());
                    if (insideHouse) {
                        setState(ghost, GhostState.LEAVING_HOUSE);
                    }
                    else {
                        setState(ghost, pacHasPower && !alreadyKilled ? GhostState.FRIGHTENED : GhostState.HUNTING_PAC);
                    }
                    ghost.houseAccess().setUnlockRequested(false);
                }
            }
            case ENTERING_HOUSE -> {
                if (ghost.houseAccess().reachedRevivalPosition()) {
                    setState(ghost, GhostState.LOCKED);
                }
            }
            case LEAVING_HOUSE -> {
                if (ghost.houseAccess().leftHouse()) {
                    setState(ghost, pacHasPower && !alreadyKilled ? GhostState.FRIGHTENED : GhostState.HUNTING_PAC);
                }
            }
            case RETURNING_HOME -> {
                if (ghost.houseAccess().reachedHouseEntry()) {
                    setState(ghost, GhostState.ENTERING_HOUSE);
                }
            }
            case HUNTING_PAC -> {
                if (pacHasPower && !alreadyKilled) {
                    setState(ghost, GhostState.FRIGHTENED);
                }
            }
            case FRIGHTENED -> {
                if (!pacHasPower) {
                    setState(ghost, GhostState.HUNTING_PAC);
                }
            }
        }
    }

    public void setState(Ghost ghost, GhostState state) {
        requireNonNull(ghost);
        requireNonNull(state);
        final GhostState oldState = ghost.state().enumValue();
        ghost.state().setEnumValue(state);
        Logger.info("Ghost {} state: {} -> {}", ghost.name(), oldState, state);
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
}
