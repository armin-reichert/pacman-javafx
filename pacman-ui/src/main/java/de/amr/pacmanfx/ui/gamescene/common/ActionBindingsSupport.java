package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.pacmanfx.ui.action.core.ActionBindingsRegistry;
import de.amr.pacmanfx.ui.action.core.GameActionBindingsMap;

public class ActionBindingsSupport implements GameSceneComponent {

    private final ActionBindingsRegistry bindingsMap = new GameActionBindingsMap("Action Bindings for " + getClass().getSimpleName());

    public ActionBindingsRegistry bindingsMap() {
        return bindingsMap;
    }
}
