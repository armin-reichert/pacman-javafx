/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GenericChangeEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;

public class CutScenesTestState extends GameState implements TestState {

    public int testedCutSceneNumber;

    public CutScenesTestState() {
        super("Cut Scenes Test State");
    }

    @Override
    public String name() {
        return "CutScenesTestState";
    }

    @Override
    public void onEnter(GameContext gameContext) {
        block();
        testedCutSceneNumber = 1;
    }

    @Override
    public void onUpdate(GameContext gameContext) {
        if (timer().hasExpired()) {
            if (testedCutSceneNumber < gameContext.rules().lastCutSceneNumber()) {
                testedCutSceneNumber += 1;
                block();
                //TODO find another solution and get rid of this event type
                gameContext.flow().publishGameEvent(new GenericChangeEvent(gameContext, "Cut Scene Test"));
            } else {
                gameContext.flow().enterState(GameStateID.GAME_INTRO);
            }
        }
    }
}
