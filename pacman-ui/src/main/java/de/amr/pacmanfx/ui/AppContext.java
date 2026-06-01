/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameFlow;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import javafx.util.Duration;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public interface AppContext extends GameUI_Life {

    GameUI ui();

    GameContext gameContext();

    GameClock gameClock();

    default String currentGameVariant() {
        return gameContext().gameVariantName();
    }

    default <T extends AbstractGameModel> T currentGame() {
        return gameContext().gameModel();
    }

    default Optional<GameLevel> optCurrentGameLevel() {
        return currentGame().optGameLevel();
    }

    default GameFlow currentGameFlow() {
        return currentGame().flow();
    }

    default State<Game> currentGameState() {
        return currentGameFlow().state();
    }

    default UIConfig currentUIConfig() {
        return ui().configurations().getOrCreateUIConfig(gameContext().gameVariantName());
    }

    default Optional<GameSoundEffects> currentSoundEffects() {
        return currentUIConfig().optSoundEffects();
    }

    /**
     * Displays a flash message.
     *
     * @param duration how long the message remains visible before fading
     * @param message  message text (supports {@link String#format})
     * @param args     formatting arguments
     */
    default void shortMessage(Duration duration, String message, Object... args) {
        requireNonNull(duration);
        requireNonNull(message);
        ui().flashMessages().showMessage(message.formatted(args), duration.toSeconds());
    }

    /**
     * Displays a flash message using the default duration.
     *
     * @param message message text
     * @param args    formatting arguments
     */
    default void shortMessage(String message, Object... args) {
        shortMessage(GameUI_Constants.DEFAULT_FLASH_MESSAGE_DURATION, message, args);
    }

}
