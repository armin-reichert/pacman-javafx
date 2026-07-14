/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.CreditAddedEvent;
import de.amr.pacmanfx.core.event.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

import java.util.Optional;

/**
 * Default event handler used by game scenes.
 */
public abstract class BaseGameSceneEventHandler extends DefaultGameEventListener {

    public abstract GameScene gameScene();

    public GameActionContext actionContext() {
        return gameScene().actionContext();
    }

    public GameContext gameContext() {
        return actionContext().currentGameContext();
    }

    public Optional<GameSoundEffects> optSoundEffects() {
        return actionContext().variants().currentVariant().config().optSoundEffects();
    }

    @Override
    public void onCreditAdded(CreditAddedEvent e) {
        optSoundEffects().ifPresent(GameSoundEffects::playCoinInsertedSound);
    }

    @Override
    public void onStopAllSounds(StopAllSoundsEvent event) {
        optSoundEffects().ifPresent(GameSoundEffects::stopAll);
    }
}
