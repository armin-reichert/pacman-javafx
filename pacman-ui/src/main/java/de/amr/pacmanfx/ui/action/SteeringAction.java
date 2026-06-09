/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.game.Game;

import static java.util.Objects.requireNonNull;

public class SteeringAction extends GameAction {

    private static String makeID(Direction dir) {
        return "steer_pac_%s".formatted(dir.name().toLowerCase());
    }

    private final Direction dir;

    public SteeringAction(Direction dir) {
        super(makeID(requireNonNull(dir)));
        this.dir = requireNonNull(dir);
    }

    @Override
    public void doAction(Game game) {
        game.currentGameContext().optCurrentLevel().ifPresent(level -> level.entities().pac().setWishDir(dir));
    }

    @Override
    public boolean isEnabled(Game game) {
        final GameLevel level = game.currentGameContext().optCurrentLevel().orElse(null);
        return level != null && !level.isDemoLevel() && !level.entities().pac().isUsingAutopilot();
    }
}