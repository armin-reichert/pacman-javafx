/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.GenericChangeEvent;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;

public class CutScenesTestState extends TestState {

    public int testedCutSceneNumber;

    @Override
    public String name() {
        return "CutScenesTestState";
    }

    @Override
    public void onEnter(GameContext context) {
        lock();
        testedCutSceneNumber = 1;
    }

    @Override
    public void onUpdate(GameContext context) {
        final GameModel game = context.game();
        if (timer.hasExpired()) {
            if (testedCutSceneNumber < game.rules().lastCutSceneNumber()) {
                testedCutSceneNumber += 1;
                lock();
                //TODO find another solution and get rid of this event type
                game.flow().publishGameEvent(new GenericChangeEvent(context, "Cut Scene Test"));
            } else {
                game.flow().enterState(GameStateID.GAME_INTRO.name());
            }
        }
    }
}
