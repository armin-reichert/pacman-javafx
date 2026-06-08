/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.app.AppConstants;
import de.amr.pacmanfx.ui.app.Game;
import de.amr.pacmanfx.ui.d3.PlayScene3D;
import org.tinylog.Logger;

public class Arcade_PlayScene3D extends PlayScene3D {

    public Arcade_PlayScene3D(Game context) {
        super(context);
    }

    @Override
    public void replaceActionBindings(GameLevel level) {
        actionBindings().dispose();
        if (level.isDemoLevel()) {
            actionBindings().registerAllBindings(ArcadePacMan_UIConfig.GAME_START_ACTION_BINDINGS);
        } else {
            actionBindings().registerAllBindings(AppConstants.STEERING_ACTION_BINDINGS);
            actionBindings().registerAllBindings(AppConstants.CHEAT_ACTION_BINDINGS);
        }
        bindActions();
        Logger.info(actionBindings());
    }
}