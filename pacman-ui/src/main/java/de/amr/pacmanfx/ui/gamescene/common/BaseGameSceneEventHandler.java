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

import static java.util.Objects.requireNonNull;

/**
 * Default event handler used by game scenes.
 */
public class BaseGameSceneEventHandler extends DefaultGameEventListener {

    private final GameScene gameScene;

    public BaseGameSceneEventHandler(GameScene gameScene) {
        this.gameScene = requireNonNull(gameScene);
    }

    public GameActionContext actionContext() {
        return gameScene.actionContext();
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
