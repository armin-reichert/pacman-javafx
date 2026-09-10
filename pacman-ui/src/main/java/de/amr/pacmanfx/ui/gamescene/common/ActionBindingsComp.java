package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsRegistry;

public class ActionBindingsComp implements GameSceneComponent {

    private final ActionBindingsRegistry registry;

    public ActionBindingsComp(Object target) {
        registry = new GameActionBindingsRegistry("Action Bindings for '%s'".formatted(target));
    }

    public ActionBindingsRegistry registry() {
        return registry;
    }
}
