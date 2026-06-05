/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.fsm.State;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import javafx.beans.property.StringProperty;
import javafx.util.Duration;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public interface AppContext extends GameContext, AppLifecycle {

    Input input();

    GameUI ui();

    GameClock gameClock();

    CoinMechanism coinMechanism();

    StringProperty gameVariantNameProperty();

    /**
     * Selects the game variant with the given name.
     * <p>
     * If no game with the specified name exists, an {@link IllegalArgumentException} is thrown.
     *
     * @param variantName the name of the game variant to select (must not be {@code null})
     * @throws IllegalArgumentException if no game with the given name is registered
     */
    void selectGameVariant(String variantName);

    /**
     * Returns the name (identifier) of the currently selected game variant.
     *
     * @return the current game variant name
     */
    String currentGameVariantName();

    /**
     * Returns the game model associated with the specified variant name.
     *
     * @param variantName the name of the game variant
     * @param <T>         the expected type of the game model
     * @return the game model for the given variant name
     * @throws ClassCastException if the registered game model cannot be cast to the expected type
     */
    <T extends GameModel> T gameForVariant(String variantName);

    default <T extends AbstractGameModel> T currentGame() {
        return gameForVariant(currentGameVariantName());
    }

    default Optional<GameLevel> optCurrentGameLevel() {
        return currentGame().optGameLevel();
    }

    @Override
    default GameModel gameModel() {
        return currentGame();
    }

    @Override
    default GameFlow gameFlow() {
        return currentGame().flow();
    }

    default State<GameContext> currentGameState() {
        return currentGame().flow().state();
    }

    default UIConfig currentUIConfig() {
        return ui().configurations().getOrCreateUIConfig(currentGameVariantName());
    }

    default Optional<GameSoundEffects> currentSoundEffects() {
        return currentUIConfig().optSoundEffects();
    }

    DirectoryWatchdog watchdog();

    PreferencesManager prefs();

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
        shortMessage(AppConstants.DEFAULT_FLASH_MESSAGE_DURATION, message, args);
    }
}
