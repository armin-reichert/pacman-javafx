/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.fsm.StateMachine;
import de.amr.pacmanfx.lib.timer.TickTimer;
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
            int lastCutSceneNumber = game.lastLevelNumber() == 32 ? 4 : 3; //TODO hack to detected Tengen, need API
            if (testedCutSceneNumber < lastCutSceneNumber) {
                testedCutSceneNumber += 1;
                timer.restartIndefinitely();
                //TODO find another solution and get rid of this event type
                game.publishGameEvent(GameEvent.Type.UNSPECIFIED_CHANGE);
            } else {
                game.control().enterStateNamed(GameControl.StateName.INTRO.name());
            }
        }
    }
}
