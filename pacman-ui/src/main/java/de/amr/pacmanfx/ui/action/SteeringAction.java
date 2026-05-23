/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;

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
    public void execute(GameUI ui) {
        ui.gameContext().game().optGameLevel().ifPresent(level -> level.pac().setWishDir(dir));
    }

    @Override
    public boolean isEnabled(GameUI ui) {
        final Game game = ui.gameContext().game();
        final GameLevel level = game.optGameLevel().orElse(null);
        return level != null && !level.isDemoLevel() && !level.pac().isUsingAutopilot();
    }
}