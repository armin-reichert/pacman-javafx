/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import javafx.scene.input.KeyCode;

import java.util.EnumMap;
import java.util.Set;

import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.bareKey;
import static de.amr.pacmanfx.ui.input.KeyCodeCombinationBuilder.combine;
import static java.util.Objects.requireNonNull;

public class SteeringActions {

    public static class SteeringAction extends GameAction {

        private static String createActionID(Direction dir) {
            return "steer_pac_%s".formatted(dir.name().toLowerCase());
        }

        private final Direction dir;

        public SteeringAction(GameActionContext actionContext, Direction dir) {
            super(actionContext, createActionID(requireNonNull(dir)));
            this.dir = requireNonNull(dir);
        }

        @Override
        public void doAction() {
            actionContext.gameContext().model().optLevel().ifPresent(level -> level.entities().pac().setWishDir(dir));
        }

        @Override
        public boolean isEnabled() {
            final GameLevel level = actionContext.gameContext().model().optLevel().orElse(null);
            return level != null && !level.isDemoLevel() && !level.entities().pac().isUsingAutopilot();
        }
    }

    private final EnumMap<Direction, GameAction> actions = new EnumMap<>(Direction.class);
    private final Set<ActionKeyBinding> bindings;

    public SteeringActions(GameActionContext game) {
        for (Direction dir : Direction.values()) {
            actions.put(dir, new SteeringAction(game, dir));
        }

        bindings = Set.of(
            new ActionKeyBinding(actions.get(Direction.UP),    bareKey(KeyCode.UP),    combine().ctrl().key(KeyCode.UP)),
            new ActionKeyBinding(actions.get(Direction.DOWN),  bareKey(KeyCode.DOWN),  combine().ctrl().key(KeyCode.DOWN)),
            new ActionKeyBinding(actions.get(Direction.LEFT),  bareKey(KeyCode.LEFT),  combine().ctrl().key(KeyCode.LEFT)),
            new ActionKeyBinding(actions.get(Direction.RIGHT), bareKey(KeyCode.RIGHT), combine().ctrl().key(KeyCode.RIGHT))
        );
    }

    public GameAction actionSteer(Direction dir) {
        requireNonNull(dir);
        return actions.get(dir);
    }

    public Set<ActionKeyBinding> bindings() {
        return bindings;
    }
}
