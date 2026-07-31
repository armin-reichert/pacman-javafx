/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.state;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.comp.ghost.GhostState;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;

import java.util.Set;

public class CommonEatingGhostState extends GameState {

    // Ghosts in these states are updated during this game state
    public static final Set<GhostState> UPDATED_GHOST_STATES_WHILE_EATEN = Set.of(
        GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE);

    public CommonEatingGhostState() {
        super(GameStateID.GAME_LEVEL_EATING_GHOST);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        timer().restartTicks(60);
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameSystems sys = gameContext.systems();

        final GameModel model = gameContext.model();
        final GameLevel level = model.optLevel().orElseThrow();

        if (timer().hasExpired()) {
            level.entities().pac().show();
            level.ghostsInState(GhostState.EATEN).forEach(
                ghost -> sys.ghostState().changeState(gameContext, ghost, GhostState.RETURNING_HOME));
            level.entities().ghosts().forEach(sys.spriteAnim()::playSelected);
            gameContext.flow().resumePreviousState(gameContext);
        }
        else {
            if (timer().tickCount() < 60) {
                level.ghostsInAnyOfStates(UPDATED_GHOST_STATES_WHILE_EATEN).forEach(
                    ghost -> ghost.update(gameContext));
                level.heartbeat().triggerPulse();
            }
        }
    }
}
