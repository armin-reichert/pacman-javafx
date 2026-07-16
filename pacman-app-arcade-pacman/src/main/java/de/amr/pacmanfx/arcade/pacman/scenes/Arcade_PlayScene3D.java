/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d3.PlayScene3D;
import org.tinylog.Logger;

public class Arcade_PlayScene3D extends PlayScene3D {

    public Arcade_PlayScene3D(GameAppContext actionContext) {
        super(actionContext);
    }

    @Override
    public void replaceActionBindings(GameLevel level) {
        actionBindings().dispose();

        final Arcade_Actions actions = appContext().getExtensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        if (level.isDemoLevel()) {
            actionBindings().registerAllBindings(actions.gameStartActionBindings());
        } else {
            actionBindings().registerAllBindings(appContext().commonActions().steeringActions().bindings());
            actionBindings().registerAllBindings(appContext().commonActions().cheatActions().bindings());
        }
        bindActions();
        Logger.info(actionBindings());
    }
}