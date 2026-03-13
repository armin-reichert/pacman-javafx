/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.action.ActionBindingsManagerImpl;
import de.amr.pacmanfx.ui.d3.Factory3D;
import de.amr.pacmanfx.ui.d3.PlayScene3D;

public class Arcade_PlayScene3D extends PlayScene3D {

    public Arcade_PlayScene3D(Factory3D factory3D) {
        super(factory3D);
    }

    @Override
    protected void replaceActionBindings(GameLevel level) {
        actionBindings = new ActionBindingsManagerImpl();
        if (level.isDemoLevel()) {
            actionBindings.registerAllFrom(ArcadePacMan_UIConfig.GAME_START_BINDINGS);
        } else {
            actionBindings.registerAllFrom(GameUI.STEERING_BINDINGS);
            actionBindings.registerAllFrom(GameUI.CHEAT_BINDINGS);
        }
        bindSceneActions();

        actionBindings.addAll(GameUI.KEYBOARD);
    }
}