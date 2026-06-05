package de.amr.pacmanfx.flow;

import de.amr.pacmanfx.core.GameContext;

public class GameIntroState extends GameState {

    public GameIntroState() {
        super("GAME_INTRO");
    }

    @Override
    public void onEnter(GameContext context) {
        context.gameModel().hud()
            .levelCounter(true)
            .livesCounter(false)
            .credit(true)
            .score(true)
            .show();

        lock();
    }

    @Override
    public void onUpdate(GameContext context) {
        if (timer().hasExpired()) {
            context.gameFlow().enterState("GAME_OR_LEVEL_STARTING");
        }
    }
}
