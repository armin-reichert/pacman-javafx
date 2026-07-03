/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_HUDState;
import org.tinylog.Logger;

public class GameOrLevelStartingState extends GameState {

    public GameOrLevelStartingState() {
        super(GameStateID.GAME_OR_LEVEL_STARTING);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        final TengenMsPacMan_GameModel gameModel = (TengenMsPacMan_GameModel) gameContext.model();
        final TengenMsPacMan_HUDState hud = gameModel.hudState();

        hud.creditOff().scoreOn().levelCounterOn().livesCounterOn().showIt();

        // The rules vary between map categories so update the rules here:
        gameModel.rules().setCurrentMapCategory(gameModel.mapCategory());

        Logger.info("Using game rules for map category {}", gameModel.rules().currentMapCategory());
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        final GameModel gameModel = gameContext.model();
        final GameFlow flow = gameContext.flow();

        if (gameModel.isPlaying()) {
            flow.enterState(GameStateID.GAME_LEVEL_CONTINUE);
        }
        else if (gameModel.canStartNewGame(gameContext)) {
            flow.enterState(GameStateID.GAME_STARTING);
        }
        else {
            flow.enterState(GameStateID.DEMO_LEVEL_PLAYING);
        }
    }
}
