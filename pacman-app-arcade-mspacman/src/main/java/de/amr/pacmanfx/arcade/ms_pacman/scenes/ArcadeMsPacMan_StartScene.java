/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.sound.SoundID;

public class ArcadeMsPacMan_StartScene extends GameScene2D {

    public ArcadeMsPacMan_StartScene() {}

    @Override
    public void doInit(Game game) {
        game.hud().credit(true).score(true).levelCounter(true).livesCounter(false).show();
        actionBindings.registerAllBindingsFrom(ArcadePacMan_UIConfig.DEFAULT_BINDINGS); // Insert coin + start game action
    }

    @Override
    protected void doEnd(Game game) {
        ui.voicePlayer().stop();
    }

    @Override
    public void update(Game game) {}

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        ui.soundManager().play(SoundID.COIN_INSERTED);
    }
}