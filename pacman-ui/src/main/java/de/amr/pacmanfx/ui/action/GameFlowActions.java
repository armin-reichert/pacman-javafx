package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.ui.game.Game;
import javafx.scene.input.KeyCode;
import org.tinylog.Logger;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.Keyboard.bare;

public class GameFlowActions {

    private final GameAction actionStartGame;
    private final GameAction actionQuit;

    private final Set<ActionKeyBinding> bindings;

    public GameFlowActions(Game game) {

        actionStartGame = new GameAction(game, "start_game") {
            @Override
            protected void doAction() {
                game.start();
            }
        };

        actionQuit = new GameAction(game, "quit") {
            @Override
            protected void doAction() {
                Logger.info("Call QUIT handler for {}", game.ui().subViews().currentView());
                game.ui().subViews().currentView().handleQuit(game);
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionStartGame, bare(KeyCode.F3)),
            new ActionKeyBinding(actionQuit,bare(KeyCode.Q))
        );
    }

    public GameAction actionQuit() {
        return actionQuit;
    }

    public GameAction actionStartGame() {
        return actionStartGame;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }
}
