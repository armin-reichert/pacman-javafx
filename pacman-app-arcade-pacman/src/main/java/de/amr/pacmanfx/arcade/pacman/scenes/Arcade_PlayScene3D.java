/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.d3.PlayScene3D;

public class Arcade_PlayScene3D extends PlayScene3D {

    public Arcade_PlayScene3D() {}

    @Override
    public void replaceActionBindings(GameLevel level) {
        actionBindings.dispose();
        if (level.isDemoLevel()) {
            actionBindings.bindAll(ArcadePacMan_UIConfig.GAME_START_ACTION_BINDINGS);
        } else {
            actionBindings.bindAll(GameUI.STEERING_ACTION_BINDINGS);
            actionBindings.bindAll(GameUI.CHEAT_ACTION_BINDINGS);
        }
        bindPlaySceneActions();
        actionBindings.pluginKeyboard();
    }
}