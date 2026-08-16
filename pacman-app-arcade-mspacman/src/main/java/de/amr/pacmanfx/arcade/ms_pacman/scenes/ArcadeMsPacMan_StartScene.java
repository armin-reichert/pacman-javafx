/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;

public class ArcadeMsPacMan_StartScene extends AbstractGameScene {

    public ArcadeMsPacMan_StartScene(GameAppContext appContext) {
        super(appContext);
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = app().currentGameVariantUIConfig()
            .getExtensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        // Insert coin + start game actions
        actionBindings().registerAllBindings(actions.gameStartActionBindings());
    }

    @Override
    public void onDeactivate() {
        app().ui().sounds().voice().stop();
        actionBindings().dispose();
    }

    @Override
    public void onTick(GameContext game) {}
}