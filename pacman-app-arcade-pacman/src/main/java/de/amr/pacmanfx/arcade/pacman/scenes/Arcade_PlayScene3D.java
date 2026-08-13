/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d3.PlayScene3D;
import org.tinylog.Logger;

public class Arcade_PlayScene3D extends PlayScene3D {

    public Arcade_PlayScene3D(GameAppContext appContext) {
        super(appContext);
    }

    @Override
    public void replaceActionBindings(GameSession session, GameLevel level) {
        actionBindings().dispose();

        final Arcade_Actions actions = app().currentGameVariantConfig()
            .getExtensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        if (session.isAttractMode()) {
            actionBindings().registerAllBindings(actions.gameStartActionBindings());
        } else {
            actionBindings().registerAllBindings(app().commonActions().steeringActions().bindings());
            actionBindings().registerAllBindings(app().commonActions().cheatActions().bindings());
        }
        bindActions();
        Logger.info(actionBindings());
    }
}