/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class ArcadeGameOverState extends GameState {

    public ArcadeGameOverState() {
        super(GameStateID.GAME_OVER);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel model = gameContext.model();
        final GameLevel level = gameContext.assertLevel();

        gameContext.gamePlay().updateHighScore(gameContext);
        gameContext.gamePlay().showLevelMessage(level, GameLevelMessageType.GAME_OVER);

        // In case, entering game over state was forced by user:
        model.setLifeCount(0);
        model.setPlaying(false);

        gameContext.cheats().clear();

        timer().restartTicks(level.gameOverStateTicks());
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        if (timer().hasExpired()) {
            final GameLevel level = gameContext.assertLevel();
            level.clearMessage();
            gameContext.cheats().clear();
            gameContext.flow().enterState(gameContext.coinMechanism().isEmpty()
                ? GameStateID.GAME_INTRO
                : GameStateID.GAME_PREPARATION);
        }
    }
}
