/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.event.GameEventListener;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

public class ArcadeMsPacMan_StartScene extends GameScene2D {

    public ArcadeMsPacMan_StartScene(GameUI ui) {
        super(ui);

        final GameEventListener gameEventHandler = new GameScene.DefaultGameEventHandler(this) {
            @Override
            public void onCreditAdded(CreditAddedEvent e) {
                soundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
            }
        };
        setGameEventHandler(gameEventHandler);
    }

    @Override
    public void onActivate() {
        actionBindings.registerAllBindingsFromSet(ArcadePacMan_UIConfig.GAME_START_ACTION_BINDINGS); // Insert coin + start game action
    }

    @Override
    public void onDeactivate() {
        ui.voicePlayer().stopVoice();
    }
}