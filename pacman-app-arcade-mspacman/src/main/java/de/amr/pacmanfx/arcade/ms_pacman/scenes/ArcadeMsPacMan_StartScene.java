/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.game.Game;

public class ArcadeMsPacMan_StartScene extends GameScene2D {

    public ArcadeMsPacMan_StartScene(Game game) {
        super(game);
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = game().extensions()
            .get(ArcadePacMan_UIConfig.EXT_ARCADE_ACTIONS, Arcade_Actions.class);

        // Insert coin + start game actions
        actionBindings().registerAllBindings(actions.gameStartActionBindings());
    }

    @Override
    public void onDeactivate() {
        game().ui().sounds().stopAndDisposeVoice();
        actionBindings().dispose();
    }
}