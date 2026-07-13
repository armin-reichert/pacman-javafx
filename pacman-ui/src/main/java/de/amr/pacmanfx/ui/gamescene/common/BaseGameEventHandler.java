/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.event.CreditAddedEvent;
import de.amr.pacmanfx.core.event.DefaultGameEventListener;
import de.amr.pacmanfx.core.event.StopAllSoundsEvent;
import de.amr.pacmanfx.core.state.GameState;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Default event handler used by scenes unless replaced.
 */
public class BaseGameEventHandler extends DefaultGameEventListener {

    private final GameActionContext actionContext;

    public BaseGameEventHandler(GameActionContext actionContext) {
        this.actionContext = requireNonNull(actionContext);
    }

    public GameActionContext actionContext() {
        return actionContext;
    }

    public GameContext gameContext() {
        return actionContext.currentGameContext();
    }

    public GameState gameState() {
        return gameContext().state();
    }

    public GameModel gameModel() {
        return gameContext().model();
    }

    public Optional<GameSoundEffects> optSoundEffects() {
        return actionContext.variants().currentVariant().config().optSoundEffects();
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
