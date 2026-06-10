/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.Globals_GameUI;
import de.amr.pacmanfx.ui.config.UIConfig;
import de.amr.pacmanfx.ui.input.Input;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import javafx.beans.property.StringProperty;
import javafx.util.Duration;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public interface Game extends GameLifecycle {

    Input input();

    GameUI ui();

    GameClock clock();

    CoinMechanism coinMechanism();

    StringProperty variantNameProperty();

    void selectGameVariant(String variantName);

    String currentGameVariantName();

    GameVariant gameVariant(String variantName);

    GameContext currentGameContext();

    UIConfig currentUIConfig();

    default Optional<GameSoundEffects> currentSoundEffects() {
        return currentUIConfig().optSoundEffects();
    }

    DirectoryWatchdog watchdog();

    PreferencesManager prefs();

    void setCollisionDoubleChecked(boolean value);

    void setCollisionStrategy(CollisionStrategy collisionStrategy);

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
        shortMessage(Globals_GameUI.DEFAULT_FLASH_MESSAGE_DURATION, message, args);
    }
}
