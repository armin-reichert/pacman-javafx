/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.action;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.app.AppContext;

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
    public void doAction(AppContext context) {
        context.currentGameContext().optCurrentGameLevel().ifPresent(level -> level.entities().pac().setWishDir(dir));
    }

    @Override
    public boolean isEnabled(AppContext context) {
        final GameLevel level = context.currentGameContext().optCurrentGameLevel().orElse(null);
        return level != null && !level.isDemoLevel() && !level.entities().pac().isUsingAutopilot();
    }
}