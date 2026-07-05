/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;

public class CommonGamePreparationState extends GameState {

    public CommonGamePreparationState() {
        super(GameStateID.GAME_PREPARATION);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel model = context.model();
        model.hudState().creditOn().scoreOn().levelCounterOn().livesCounterOff().showIt();
        context.gamePlay().resetForNewGame(context.model());
    }

    @Override
    public void onUpdate(GameContext context) {
        // Wait for user interaction (e.g. key press) to start playing
    }
}
