/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.CreditAddedEvent;
import de.amr.pacmanfx.core.event.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

import java.util.Optional;

/**
 * Default event handler used by game scenes.
 */
public interface GameSceneGameEventHandler extends DefaultGameEventListener {

    GameScene gameScene();

    default GameAppContext actionContext() {
        return gameScene().actionContext();
    }

    default GameContext gameContext() {
        return actionContext().currentGameContext();
    }

    default Optional<GameSoundEffects> optSoundEffects() {
        return actionContext().variants().currentVariant().config().optSoundEffects();
    }

    @Override
    default void onCreditAdded(CreditAddedEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
    }

    @Override
    default void onStopAllSounds(StopAllSoundsEvent event) {
        optSoundEffects().ifPresent(GameSoundEffects::stopAll);
    }
}
