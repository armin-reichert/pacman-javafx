/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.core.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.GenericChangeEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;

public class Test_CutScenesTestState extends AbstractGameState {

    public int testedCutSceneNumber;

    public Test_CutScenesTestState() {
        super(TestStateID.CUT_SCENE_TEST);
    }

    @Override
    public void onEnterState(GameContext game) {
        testedCutSceneNumber = 1;
        timer().resetToIndefiniteDuration();
    }

    @Override
    public void onUpdate(GameContext game) {
        if (timer().hasExpired()) {
            if (testedCutSceneNumber < rules.lastCutSceneNumber()) {
                testedCutSceneNumber += 1;
                timer().resetToIndefiniteDuration();
                //TODO find another solution and get rid of this event type
                game.eventManager().publishGameEvent(new GenericChangeEvent("Cut Scene Test"));
            } else {
                flow.enterGameState(game, CommonGameStateID.GAME_INTRO);
            }
        }
    }
}
