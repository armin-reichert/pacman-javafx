/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.level.GameLevel;

import java.util.Set;

public class GameLevelEatingGhostState extends GameState {

    public GameLevelEatingGhostState() {
        super(GameStateID.GAME_LEVEL_EATING_GHOST);
    }

    @Override
    public void onEnter(GameContext context) {
        timer().restartTicks(60);
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel game = context.gameModel();
        final GameLevel level = game.optGameLevel().orElseThrow();
        if (timer().hasExpired()) {
            level.entities().pac().show();
            level.ghostsInState(GhostState.EATEN).forEach(ghost -> ghost.setState(GhostState.RETURNING_HOME));
            level.entities().ghosts().forEach(ghost -> ghost.animations().playSelected());
            context.gameFlow().resumePreviousState();
        } else {
            if (timer().tickCount() < 60) {
                level.ghostsInAnyOfStates(Set.of(GhostState.EATEN, GhostState.RETURNING_HOME, GhostState.ENTERING_HOUSE))
                    .forEach(ghost -> ghost.update(level));
                level.heartbeat().triggerPulse();
            }
        }
    }
}
