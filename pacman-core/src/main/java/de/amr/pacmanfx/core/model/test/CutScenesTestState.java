/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GenericChangeEvent;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.state.GameStateID;

public class CutScenesTestState extends GameState {

    public int testedCutSceneNumber;

    public CutScenesTestState() {
        super(TestStateID.CUT_SCENE_TEST);
    }

    @Override
    public void onEnter(GameContext gameContext) {
        testedCutSceneNumber = 1;
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        if (timer().hasExpired()) {
            if (testedCutSceneNumber < gameContext.model().rules().lastCutSceneNumber()) {
                testedCutSceneNumber += 1;
                waitForTimeout();
                //TODO find another solution and get rid of this event type
                gameContext.eventManager().publishGameEvent(new GenericChangeEvent("Cut Scene Test"));
            } else {
                gameContext.flow().enterState(gameContext, GameStateID.GAME_INTRO);
            }
        }
    }
}
