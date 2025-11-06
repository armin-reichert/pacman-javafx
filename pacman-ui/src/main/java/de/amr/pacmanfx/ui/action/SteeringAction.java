/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.action;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.ui.api.GameUI;

import static java.util.Objects.requireNonNull;

public class SteeringAction extends GameAction {
    private final Direction dir;

    public SteeringAction(Direction dir) {
        super("STEER_PAC_" + dir);
        this.dir = requireNonNull(dir);
    }

    @Override
    public void execute(GameUI ui) {
        ui.gameContext().gameLevel().pac().setWishDir(dir);
    }

    @Override
    public boolean isEnabled(GameUI ui) {
        return ui.gameContext().optGameLevel().isPresent() && !ui.gameContext().gameLevel().pac().isUsingAutopilot();
    }
}