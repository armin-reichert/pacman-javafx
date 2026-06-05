/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;

public class GameLevelPacManDyingState extends GameState {

    public GameLevelPacManDyingState() {
        super(GameStateID.GAME_LEVEL_PACMAN_DYING);
    }

    @Override
    public void onEnter(GameContext context) {
        lock(); // UI triggers time-out
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel game = context.gameModel();
        final GameLevel level = game.optGameLevel().orElseThrow();
        if (timer().hasExpired()) {
            if (level.isDemoLevel()) {
                context.gameFlow().enterState(GameStateID.GAME_OVER);
            } else {
                game.lives().add(-1);
                context.gameFlow().enterState(game.lives().count() == 0
                    ? GameStateID.GAME_OVER
                    : GameStateID.GAME_OR_LEVEL_STARTING);
            }
        } else {
            game.doPacManDying(level, level.entities().pac(), timer().tickCount());
        }
    }
}
