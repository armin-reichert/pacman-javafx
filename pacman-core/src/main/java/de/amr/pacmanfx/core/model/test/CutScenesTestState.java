/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GenericChangeEvent;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.gamestate.GameState;

public class CutScenesTestState extends GameState {

    public int testedCutSceneNumber;

    public CutScenesTestState() {
        super(TestStateID.CUT_SCENE_TEST);
    }

    @Override
    public void onEnter(GameContext game) {
        testedCutSceneNumber = 1;
        waitForTimeout();
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            if (testedCutSceneNumber < game.rules().lastCutSceneNumber()) {
                testedCutSceneNumber += 1;
                waitForTimeout();
                //TODO find another solution and get rid of this event type
                game.eventManager().publishGameEvent(new GenericChangeEvent("Cut Scene Test"));
            } else {
                game.session().gameFlow().enterState(game, CommonGameStateID.GAME_INTRO);
            }
        }
    }
}
