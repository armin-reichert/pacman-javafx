/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.scene.SubScene;

import java.util.Optional;

public interface GameFacade {

    Game game();

    default GameContext gameContext() {
        return game().currentGameContext();
    }

    default GameModel gameModel() {
        return gameContext().model();
    }

    default GameState gameState() {
        return gameContext().state();
    }

    default Optional<SubScene> optSubSceneFX() {
        return Optional.empty();
    }

    default Optional<GameSoundEffects> optSoundEffects() {
        return game().currentSoundEffects();
    }

}
