/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.action.SimpleActionBindingsManager;

public class Arcade_PlayScene3D extends PlayScene3D {

    public Arcade_PlayScene3D() {}

    @Override
    protected void replaceActionBindings(GameLevel level) {
        actionBindings = new SimpleActionBindingsManager();

        if (level.isDemoLevel()) {
            actionBindings.registerAllFrom(ArcadePacMan_UIConfig.GAME_START_BINDINGS);
        } else {
            actionBindings.registerAllFrom(GameUI.STEERING_BINDINGS);
            actionBindings.registerAllFrom(GameUI.CHEAT_BINDINGS);
        }
        actionBindings.registerAllFrom(_3D_BINDINGS);

        actionBindings.addAll(GameUI.KEYBOARD);
    }
}