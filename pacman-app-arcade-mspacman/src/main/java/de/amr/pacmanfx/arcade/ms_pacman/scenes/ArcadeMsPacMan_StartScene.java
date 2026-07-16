/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;

public class ArcadeMsPacMan_StartScene extends AbstractGameScene2D {

    public ArcadeMsPacMan_StartScene(GameActionContext actionContext) {
        super(actionContext);
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = actionContext().getExtensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        // Insert coin + start game actions
        actionBindings().registerAllBindings(actions.gameStartActionBindings());
    }

    @Override
    public void onDeactivate() {
        actionContext().ui().sounds().stopAndDisposeVoice();
        actionBindings().dispose();
    }

    @Override
    public void onTick(GameContext gameContext) {}
}