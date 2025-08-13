package de.amr.pacmanfx.controller.teststates;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.controller.GamePlayState;
import de.amr.pacmanfx.controller.GameState;
import de.amr.pacmanfx.event.GameEventType;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.model.AbstractGameModel;

public class CutScenesTestState implements GameState {

    private final TickTimer timer = new TickTimer("Timer_" + name());

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
            gameModel.testedCutSceneNumber = 1;
        }
    }

    @Override
    public void onUpdate(GameContext context) {
        if (context.game() instanceof AbstractGameModel gameModel) {
            if (timer.hasExpired()) {
                int lastCutSceneNumber = context.gameController().isSelected("MS_PACMAN_TENGEN") ? 4 : 3;
                if (gameModel.testedCutSceneNumber < lastCutSceneNumber) {
                    gameModel.testedCutSceneNumber += 1;
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
