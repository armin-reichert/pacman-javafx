/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.core.model.level.GameLevelMessageType;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;

public class GameOverState extends GameState {

    public GameOverState() {
        super(GameStateID.GAME_OVER);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel model = gameContext.model();
        final GameLevel level = model.assertLevel();
        final TengenMsPacMan_GamePlay game = (TengenMsPacMan_GamePlay) gameContext.gamePlay();

        model.setPlaying(false);
        model.setLifeCount(0); // Needed if state entry was triggered by user interaction

        game.updateHighScore(gameContext);
        game.showLevelMessage(level, GameLevelMessageType.GAME_OVER);

        gameContext.cheats().clear();

        //TODO rethink this
        timer().restartTicks(level.gameOverStateTicks());
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final TengenMsPacMan_GameModel model = (TengenMsPacMan_GameModel) gameContext.model();
        final GameLevel level = model.assertLevel();

        if (timer().hasExpired()) {
            level.clearMessage();

            final Identifier nextStateID = level.isDemoLevel()
                ? TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME
                : model.canContinueOnGameOver() ? GameStateID.GAME_PREPARATION : GameStateID.GAME_INTRO;

            gameContext.flow().enterState(nextStateID);
        }
    }
}
