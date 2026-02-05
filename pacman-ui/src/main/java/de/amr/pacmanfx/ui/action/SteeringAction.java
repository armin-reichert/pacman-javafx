/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.ui.GameUI;

import static java.util.Objects.requireNonNull;

public class SteeringAction extends GameAction {
    private final Direction dir;

    public SteeringAction(Direction dir) {
        super("STEER_PAC_" + dir);
        this.dir = requireNonNull(dir);
    }

    @Override
    public void execute(GameUI ui) {
        ui.gameContext().currentGame().level().pac().setWishDir(dir);
    }

    @Override
    public boolean isEnabled(GameUI ui) {
        return ui.gameContext().currentGame().optGameLevel().isPresent()
            && !ui.gameContext().currentGame().level().pac().isUsingAutopilot();
    }
}