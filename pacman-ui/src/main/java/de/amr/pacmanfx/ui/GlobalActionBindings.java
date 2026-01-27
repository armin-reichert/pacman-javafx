/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.action.DefaultActionBindingsManager;

public class GlobalActionBindings extends DefaultActionBindingsManager {

    public GlobalActionBindings() {
        useAnyBinding(CommonGameActions.ACTION_ENTER_FULLSCREEN, GameUI.COMMON_BINDINGS);
        useAnyBinding(CommonGameActions.ACTION_OPEN_EDITOR,      GameUI.COMMON_BINDINGS);
        useAnyBinding(CommonGameActions.ACTION_TOGGLE_MUTED,     GameUI.COMMON_BINDINGS);
        activateBindings(GameUI.KEYBOARD);
    }
}
