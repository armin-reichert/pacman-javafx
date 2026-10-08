/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.playscene;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.ui.gamescene.playscene.PlayScene3D;
import org.tinylog.Logger;

public class Arcade_PlayScene3D extends PlayScene3D {

    public Arcade_PlayScene3D() {}

    @Override
    public void replaceActionBindings(GameSession session, GameLevel level) {
        final var bindingsMap = actionBindings().registry();

        bindingsMap.dispose();

        final Arcade_Actions actions = engine().gameVariantManager().currentRuntime()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        if (session.isAttractMode()) {
            bindingsMap.registerAllBindings(actions.gameStartActionBindings());
        } else {
            bindingsMap.registerAllBindings(engine().commonActions().steeringActions().bindings());
            bindingsMap.registerAllBindings(engine().commonActions().cheatActions().bindings());
        }
        registerActionBindings();
        Logger.info(actionBindings());
    }
}