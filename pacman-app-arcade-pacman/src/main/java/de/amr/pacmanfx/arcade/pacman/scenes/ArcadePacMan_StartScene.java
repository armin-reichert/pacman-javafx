/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.sound.SoundID;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene2D {

    public ArcadePacMan_StartScene() {}

    @Override
    public void doInit(Game game) {
        game.hud().credit(true).score(true).levelCounter(true).livesCounter(false).show();
        actionBindings.useAllBindings(ArcadePacMan_UIConfig.DEFAULT_BINDINGS);
    }

    @Override
    protected void doEnd(Game game) {
        soundManager().stopVoice();
    }

    @Override
    public void update(Game game) {}

    @Override
    public void onCreditAdded(GameEvent e) {
        soundManager().play(SoundID.COIN_INSERTED);
    }
}