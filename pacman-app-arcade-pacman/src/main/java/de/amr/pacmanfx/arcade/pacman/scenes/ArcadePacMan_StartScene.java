/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.ui.gamescene.GameScene;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene2D {

    public ArcadePacMan_StartScene(AppContext ui) {
        super(ui);

        final var gameEventHandler = new GameScene.DefaultGameEventHandler(this) {
            @Override
            public void onCreditAdded(CreditAddedEvent e) {
                context().currentSoundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
            }
        };
        setGameEventHandler(gameEventHandler);
    }

    @Override
    public void onActivate(UIConfig uiConfig) {
        actionBindings.registerAllBindings(ArcadePacMan_UIConfig.GAME_START_ACTION_BINDINGS);
    }

    @Override
    public void onDeactivate() {
        context.ui().sounds().stopAndDisposeVoice();
    }
}