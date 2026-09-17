/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsRegistry;

public record ActionBindingsComp(ActionBindingsRegistry registry) implements GameSceneComponent {

    public ActionBindingsComp(GameScene gameScene) {
        this(new GameActionBindingsRegistry("Action Bindings for '%s'".formatted(gameScene)));
    }
}
