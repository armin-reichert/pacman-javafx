package de.amr.pacmanfx.ui.action;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.ui.game.Game;
import javafx.scene.input.KeyCode;

import java.util.Set;

import static de.amr.pacmanfx.ui.input.Keyboard.bare;
import static de.amr.pacmanfx.ui.input.Keyboard.control;

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
            new ActionKeyBinding(actionSteerUp, bare(KeyCode.UP), control(KeyCode.UP)),
            new ActionKeyBinding(actionSteerDown, bare(KeyCode.DOWN), control(KeyCode.DOWN)),
            new ActionKeyBinding(actionSteerLeft, bare(KeyCode.LEFT), control(KeyCode.LEFT)),
            new ActionKeyBinding(actionSteerRight, bare(KeyCode.RIGHT), control(KeyCode.RIGHT))
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
