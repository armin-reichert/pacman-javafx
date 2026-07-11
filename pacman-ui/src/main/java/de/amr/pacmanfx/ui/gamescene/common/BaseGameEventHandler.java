/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.CreditAddedEvent;
import de.amr.pacmanfx.event.DefaultGameEventListener;
import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Default event handler used by scenes unless replaced.
 */
public class BaseGameEventHandler extends DefaultGameEventListener {

    private final Game game;

    public BaseGameEventHandler(Game game) {
        this.game = requireNonNull(game);
    }

    public Game game() {
        return game;
    }

    public GameContext gameContext() {
        return game.context();
    }

    public GameState gameState() {
        return gameContext().state();
    }

    public GameModel gameModel() {
        return gameContext().model();
    }

    public Optional<GameSoundEffects> optSoundEffects() {
        return game.variants().currentVariant().config().optSoundEffects();
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
