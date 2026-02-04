/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._3d.PlayScene3D;

public class Arcade_PlayScene3D extends PlayScene3D {

    public Arcade_PlayScene3D() {}

    @Override
    protected void replaceActionBindings(GameLevel level) {
        actionBindings.removeAllBindings(GameUI.KEYBOARD);

        if (level.isDemoLevel()) {
            actionBindings.registerAllBindingsFrom(ArcadePacMan_UIConfig.DEFAULT_BINDINGS);
        } else {
            actionBindings.registerAllBindingsFrom(GameUI.STEERING_BINDINGS);
            actionBindings.registerAllBindingsFrom(GameUI.CHEAT_BINDINGS);
        }
        actionBindings.registerAllBindingsFrom(_3D_BINDINGS);

        actionBindings.activateBindings(GameUI.KEYBOARD);
    }
}