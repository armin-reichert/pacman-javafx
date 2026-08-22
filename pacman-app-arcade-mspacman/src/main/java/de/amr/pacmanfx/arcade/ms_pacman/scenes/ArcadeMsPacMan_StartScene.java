/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;

public class ArcadeMsPacMan_StartScene extends GameScene {

    public ArcadeMsPacMan_StartScene(GameAppContext appContext) {
        super(appContext);
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = app().currentGameVariantUIConfig()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        final var bindingsMap = actionBindingsSupport().bindingsMap();
        // Insert coin + start game actions
        bindingsMap.registerAllBindings(actions.gameStartActionBindings());
    }

    @Override
    public void onDeactivate() {
        app().ui().sounds().voice().stop();
    }

    @Override
    public void onTick(GameContext game) {}
}