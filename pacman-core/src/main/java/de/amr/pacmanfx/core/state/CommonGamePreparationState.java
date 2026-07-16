/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.state;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;

public class CommonGamePreparationState extends GameState {

    public CommonGamePreparationState() {
        super(GameStateID.GAME_PREPARATION);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final GameModel model = gameContext.model();
        model.hudState().showCredit().showScore().showLevelCounter().hideLivesCounter().show();
        gameContext.gamePlay().resetForNewGame(gameContext);
    }

    @Override
    public void onUpdate(GameContext context) {
        // Wait for user interaction (e.g. key press) to start playing
    }
}
