/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.event.GenericChangeEvent;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameFlow;

public class CutScenesTestState<GAME extends Game> extends TestState<GAME> {

    public int testedCutSceneNumber;

    @Override
    public void onEnter(GAME game) {
        lock();
        testedCutSceneNumber = 1;
    }

    @Override
    public void onUpdate(GAME game) {
        if (timer.hasExpired()) {
            if (testedCutSceneNumber < game.lastCutSceneNumber()) {
                testedCutSceneNumber += 1;
                lock();
                //TODO find another solution and get rid of this event type
                game.flow().publishGameEvent(new GenericChangeEvent(game, "Cut Scene Test"));
            } else {
                game.flow().enterStateWithName(GameFlow.CanonicalGameState.INTRO.name());
            }
        }
    }
}
