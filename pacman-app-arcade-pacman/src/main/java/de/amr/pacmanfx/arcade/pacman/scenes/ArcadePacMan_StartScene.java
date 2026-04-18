/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene2D {

    public ArcadePacMan_StartScene(GameUI ui) {
        super(ui);
    }

    @Override
    public void onSceneStart() {
        actionBindings.addAll(ArcadePacMan_UIConfig.GAME_START_ACTION_BINDINGS);
    }

    @Override
    public void onSceneEnd() {
        ui.voicePlayer().stopVoice();
    }

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        soundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
    }
}