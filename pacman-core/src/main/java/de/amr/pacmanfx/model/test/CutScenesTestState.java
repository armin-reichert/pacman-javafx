/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.event.UnspecifiedChangeEvent;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameControl;

public class CutScenesTestState implements StateMachine.State<Game>, TestState {

    private final TickTimer timer = new TickTimer("Timer_" + name());

    public int testedCutSceneNumber;

    @Override
    public String name() {
        return getClass().getSimpleName();
    }

    @Override
    public TickTimer timer() {
        return timer;
    }

    @Override
    public void onEnter(Game game) {
        timer.restartIndefinitely();
        testedCutSceneNumber = 1;
    }

    @Override
    public void onUpdate(Game game) {
        if (timer.hasExpired()) {
            if (testedCutSceneNumber < game.lastCutSceneNumber()) {
                testedCutSceneNumber += 1;
                timer.restartIndefinitely();
                //TODO find another solution and get rid of this event type
                game.publishGameEvent(new UnspecifiedChangeEvent("Cut Scene Test"));
            } else {
                game.control().enterStateNamed(GameControl.StateName.INTRO.name());
            }
        }
    }
}
