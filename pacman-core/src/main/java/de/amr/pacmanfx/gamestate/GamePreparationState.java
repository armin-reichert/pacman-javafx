package de.amr.pacmanfx.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;

public class GamePreparationState extends GameState {

    public GamePreparationState() {
        super(GameStateID.GAME_PREPARATION);
    }

    @Override
    public void onEnter(GameContext context) {
        final GameModel gameModel = context.model();
        gameModel.hudState().creditOn().scoreOn().levelCounterOn().livesCounterOff().showIt();
        gameModel.resetForNewGame();
    }

    @Override
    public void onUpdate(GameContext context) {
        // Wait for user interaction (e.g. key press) to start playing
    }

}
