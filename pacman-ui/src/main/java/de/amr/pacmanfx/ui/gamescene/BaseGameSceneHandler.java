/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.DefaultGameEventListener;
import de.amr.pacmanfx.event.StopAllSoundsEvent;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Default event handler used by scenes unless replaced. Handles generic UI updates and global sound stop events.
 */
public class BaseGameSceneHandler extends DefaultGameEventListener {

    private final Game appContext;

    public BaseGameSceneHandler(Game appContext) {
        this.appContext = requireNonNull(appContext);
    }

    public Game appContext() {
        return appContext;
    }

    public GameContext gameContext() {
        return appContext.currentGameContext();
    }

    public GameState gameState() {
        return gameContext().state();
    }

    public GameModel gameModel() {
        return gameContext().model();
    }

    public Optional<GameLevel> optGameLevel() {
        return gameContext().optCurrentLevel();
    }

    public Optional<GameSoundEffects> optSoundEffects() {
        return appContext.currentSoundEffects();
    }

    @Override
    public void onStopAllSounds(StopAllSoundsEvent event) {
        optSoundEffects().ifPresent(GameSoundEffects::stopAll);
    }
}
