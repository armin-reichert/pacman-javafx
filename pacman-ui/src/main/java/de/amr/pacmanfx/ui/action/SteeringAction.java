/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class SteeringAction extends GameAction {
    private final Direction dir;

    public SteeringAction(Direction dir) {
        super("STEER_PAC_" + dir);
        this.dir = requireNonNull(dir);
    }

    @Override
    public void execute(GameUI ui) {
        final GameLevel level = ui.gameContext().game().optGameLevel().orElseThrow();
        level.pac().setWishDir(dir);
    }

    @Override
    public boolean isEnabled(GameUI ui) {
        final Game game = ui.gameContext().game();
        final Optional<GameLevel> optGameLevel = game.optGameLevel();
        return optGameLevel.isPresent() && !optGameLevel.get().pac().isUsingAutopilot();
    }
}