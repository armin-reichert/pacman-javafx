/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.controller.test;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.AbstractGameModel;

public class CutScenesTestState implements TestGameState {

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
        if (context.game() instanceof AbstractGameModel gameModel) {
            testedCutSceneNumber = 1;
        }
    }

    @Override
    public void onUpdate(GameContext context) {
        if (context.game() instanceof AbstractGameModel gameModel) {
            if (timer.hasExpired()) {
                int lastCutSceneNumber = context.gameController().isSelected("MS_PACMAN_TENGEN") ? 4 : 3;
                if (testedCutSceneNumber < lastCutSceneNumber) {
                    testedCutSceneNumber += 1;
                    timer.restartIndefinitely();
                    //TODO find another solution and get rid of this event type
                    context.eventManager().publishEvent(GameEventType.UNSPECIFIED_CHANGE);
                } else {
                    context.gameController().changeGameState(GamePlayState.INTRO);
                }
            }
        }
    }
}
