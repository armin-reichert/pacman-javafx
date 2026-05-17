/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.ui.GameScene;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

public class ArcadeMsPacMan_StartScene extends GameScene2D {

    private static class GameEventHandler extends GameScene.DefaultGameEventHandler {

        public GameEventHandler(GameScene gameScene) {
            super(gameScene);
        }

        @Override
        public void onCreditAdded(CreditAddedEvent e) {
            gameScene().soundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
        }
    }

    public ArcadeMsPacMan_StartScene(GameUI ui) {
        super(ui);
        setGameEventHandler(new GameEventHandler(this));
    }

    @Override
    public void onSceneStart() {
        actionBindings.addAll(ArcadePacMan_UIConfig.GAME_START_ACTION_BINDINGS); // Insert coin + start game action
    }

    @Override
    public void onSceneEnd() {
        ui.voicePlayer().stopVoice();
    }
}