package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.ui.game.Game;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.Keyboard.bare;

public class GameFlowActions {

    private final GameAction actionStartGame;

    private final Set<ActionKeyBinding> bindings;

    public GameFlowActions(Game game) {

        actionStartGame = new GameAction(game, "start_game") {
            @Override
            protected void doAction() {
                game.start();
            }
        };

        bindings = Set.of(
            new ActionKeyBinding(actionStartGame, bare(KeyCode.F3))
        );
    }

    public GameAction actionStartGame() {
        return actionStartGame;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }
}
