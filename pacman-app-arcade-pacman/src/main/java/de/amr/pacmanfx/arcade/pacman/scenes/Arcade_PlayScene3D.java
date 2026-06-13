/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.d3.PlayScene3D;
import org.tinylog.Logger;

public class Arcade_PlayScene3D extends PlayScene3D {

    public Arcade_PlayScene3D(Game game) {
        super(game);
    }

    @Override
    public void replaceActionBindings(GameLevel level) {
        actionBindings().dispose();

        final Arcade_Actions actions = game().extensions()
            .get(ArcadePacMan_UIConfig.EXT_ARCADE_ACTIONS, Arcade_Actions.class);

        if (level.isDemoLevel()) {
            actionBindings().registerAllBindings(actions.gameStartActionBindings());
        } else {
            actionBindings().registerAllBindings(game().actions().steeringActions().bindings());
            actionBindings().registerAllBindings(game().actions().cheatActions().bindings());
        }
        bindActions();
        Logger.info(actionBindings());
    }
}