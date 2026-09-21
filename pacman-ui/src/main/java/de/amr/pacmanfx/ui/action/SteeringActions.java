/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.entities.WorldNavigationSystem;
import de.amr.pacmanfx.ui.action.core.ActionKeyBinding;
import de.amr.pacmanfx.ui.action.core.GameAction;
import de.amr.pacmanfx.ui.action.core.GameApp;
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

        public SteeringAction(Direction dir) {
            super(createActionID(requireNonNull(dir)));
            this.dir = requireNonNull(dir);
        }

        @Override
        public void execute(GameApp app) {
            final GameContext game = app.game();
            final WorldNavigationSystem navigator = game.playConfig().systems().navigator();
            game.session().optLevel().ifPresent(level -> navigator.setWishDir(level.entitySet().pac(), dir));
        }

        @Override
        public boolean isEnabled(GameApp app) {
            final GameSession session = app.game().session();
            return session.optLevel().isPresent()
                && !session.isAttractMode()
                && !session.level().entitySet().pac().cheats().isUsingAutopilot();
        }
    }

    private final EnumMap<Direction, GameAction> actions = new EnumMap<>(Direction.class);
    private final Set<ActionKeyBinding> bindings;

    public SteeringActions() {
        for (Direction dir : Direction.values()) {
            actions.put(dir, new SteeringAction(dir));
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
