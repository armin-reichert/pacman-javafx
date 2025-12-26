/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_HeadsUpDisplay;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene2D {

    public ArcadePacMan_StartScene(GameUI ui) {
        super(ui);
    }

    @Override
    public void doInit(Game game) {
        final Arcade_HeadsUpDisplay arcadeHud = (Arcade_HeadsUpDisplay) game.hud();
        arcadeHud.credit(true).score(true).levelCounter(true).livesCounter(false).show();
        actionBindings.useAll(ArcadePacMan_UIConfig.DEFAULT_BINDINGS);
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