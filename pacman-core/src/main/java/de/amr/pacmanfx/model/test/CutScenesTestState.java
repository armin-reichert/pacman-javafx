/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GenericChangeEvent;
import de.amr.pacmanfx.gamestate.GameStateID;

public class CutScenesTestState extends TestState {

    public int testedCutSceneNumber;

    @Override
    public String name() {
        return "CutScenesTestState";
    }

    @Override
    public void onEnter(GameContext gameContext) {
        lock();
        testedCutSceneNumber = 1;
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        if (timer.hasExpired()) {
            if (testedCutSceneNumber < gameContext.gameRules().lastCutSceneNumber()) {
                testedCutSceneNumber += 1;
                lock();
                //TODO find another solution and get rid of this event type
                gameContext.gameFlow().publishGameEvent(new GenericChangeEvent(gameContext, "Cut Scene Test"));
            } else {
                gameContext.gameFlow().enterState(GameStateID.GAME_INTRO.name());
            }
        }
    }
}
