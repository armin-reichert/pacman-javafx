/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.ui.game.PacManGamesCollection;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends AbstractGameScene2D {

    public ArcadePacMan_StartScene(PacManGamesCollection game) {
        super(game);
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = game().variants().currentVariant()
            .getExtensionValue(game(), Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);
        actionBindings().registerAllBindings(actions.gameStartActionBindings());
    }

    @Override
    public void onDeactivate() {
        game().ui().sounds().stopAndDisposeVoice();
        actionBindings().dispose();
    }

    @Override
    public void onTick(long tick) {
    }
}