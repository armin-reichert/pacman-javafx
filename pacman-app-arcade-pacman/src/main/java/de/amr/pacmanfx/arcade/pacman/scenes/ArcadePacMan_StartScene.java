/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.gamescene.BaseGameSceneHandler;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene2D {

    public ArcadePacMan_StartScene(Game game) {
        super(game);

        final var gameEventHandler = new BaseGameSceneHandler(game) {
            @Override
            public void onCreditAdded(CreditAddedEvent e) {
                optSoundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
            }
        };
        setGameEventHandler(gameEventHandler);
    }

    @Override
    public void onActivate() {
        actionBindings().registerAllBindings(ArcadePacMan_UIConfig.GAME_START_ACTION_BINDINGS);
    }

    @Override
    public void onDeactivate() {
        game().ui().sounds().stopAndDisposeVoice();
    }
}