/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.test;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.fsm.FsmState;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameState;

import static de.amr.pacmanfx.Globals.THE_GAME_BOX;

public class CutScenesTestState implements FsmState<GameContext>, TestState {

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
    public void onEnter(GameContext context) {
        timer.restartIndefinitely();
        if (context.currentGame() instanceof AbstractGameModel) {
            testedCutSceneNumber = 1;
        }
    }

    @Override
    public void onUpdate(GameContext context) {
        if (context.currentGame() instanceof AbstractGameModel) {
            if (timer.hasExpired()) {
                int lastCutSceneNumber = THE_GAME_BOX.isCurrentGameVariant("MS_PACMAN_TENGEN") ? 4 : 3;
                if (testedCutSceneNumber < lastCutSceneNumber) {
                    testedCutSceneNumber += 1;
                    timer.restartIndefinitely();
                    //TODO find another solution and get rid of this event type
                    context.currentGame().publishEvent(GameEventType.UNSPECIFIED_CHANGE);
                } else {
                    context.currentGame().stateMachine().changeState(context, GameState.INTRO);
                }
            }
        }
    }
}
