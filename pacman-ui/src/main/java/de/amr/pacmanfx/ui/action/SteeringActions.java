package de.amr.pacmanfx.ui.action;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.ui.game.Game;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;

public class SteeringActions {

    private final GameAction actionSteerUp;
    private final GameAction actionSteerDown;
    private final GameAction actionSteerLeft;
    private final GameAction actionSteerRight;

    private final Set<ActionKeyBinding> bindings;

    public SteeringActions(Game game) {
        actionSteerUp = new SteeringAction(game, Direction.UP);
        actionSteerDown = new SteeringAction(game, Direction.DOWN);
        actionSteerLeft = new SteeringAction(game, Direction.LEFT);
        actionSteerRight = new SteeringAction(game, Direction.RIGHT);

        bindings = Set.of(
            new ActionKeyBinding(actionSteerUp,    bareKey(KeyCode.UP),    combine().ctrl().key(KeyCode.UP)),
            new ActionKeyBinding(actionSteerDown,  bareKey(KeyCode.DOWN),  combine().ctrl().key(KeyCode.DOWN)),
            new ActionKeyBinding(actionSteerLeft,  bareKey(KeyCode.LEFT),  combine().ctrl().key(KeyCode.LEFT)),
            new ActionKeyBinding(actionSteerRight, bareKey(KeyCode.RIGHT), combine().ctrl().key(KeyCode.RIGHT))
        );
    }

    public GameAction actionSteerUp() {
        return actionSteerUp;
    }

    public GameAction actionSteerDown() {
        return actionSteerDown;
    }

    public GameAction actionSteerLeft() {
        return actionSteerLeft;
    }

    public GameAction actionSteerRight() {
        return actionSteerRight;
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }
}
