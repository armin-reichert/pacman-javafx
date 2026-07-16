/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.flow;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_HUDState;
import org.tinylog.Logger;

public class GameOrLevelStartingState extends GameState {

    public GameOrLevelStartingState() {
        super(GameStateID.GAME_OR_LEVEL_STARTING);
    }

    @Override
    public void onEnter(GameContext context) {
        final TengenMsPacMan_GameModel model = (TengenMsPacMan_GameModel) context.model();
        final TengenMsPacMan_HUDState hud = model.hudState();

        hud.hideCredit().showScore().showLevelCounter().showLivesCounter().show();

        // The rules vary between map categories so update the rules here:
        model.rules().setCurrentMapCategory(model.mapCategory());

        Logger.info("Using game rules for map category {}", model.rules().currentMapCategory());
    }

    @Override
    public void onUpdate(GameContext context) {
        if (!(context.model() instanceof TengenMsPacMan_GameModel model)) {
            throw new IllegalStateException("Illegal game model: " + context.model());
        }
        context.flow().enterState(context, computeNextState(model));
    }

    private GameStateID computeNextState(TengenMsPacMan_GameModel model) {
        if (model.isPlaying()) {
            return GameStateID.GAME_LEVEL_CONTINUE;
        }
        if (model.canStartNewGame()) {
            return GameStateID.GAME_STARTING;
        }
        return GameStateID.DEMO_LEVEL_PLAYING;
    }
}
