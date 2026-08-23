/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.ui.gamescene.d2.CanvasRenderingComp;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene {

    public ArcadePacMan_StartScene(GameAppContext app) {
        super(app);
        componentsRegistry().setComp(CanvasRenderingComp.class, new CanvasRenderingComp());
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = app().currentGameVariantUIConfig()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        final var bindingsMap = actionBindingsSupport().bindingsMap();
        bindingsMap.registerAllBindings(actions.gameStartActionBindings());
    }

    @Override
    public void onDeactivate() {
        app().ui().sounds().voice().stop();
    }

    @Override
    public void onTick(GameContext game) {
    }
}