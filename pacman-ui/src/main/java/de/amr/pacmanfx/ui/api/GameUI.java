/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.ui.ActionBinding;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Set;

public interface GameUI extends GameUI_Lifecycle, GameUI_ConfigManager, GameUI_ViewAccess, GameUI_SceneAccess {

    /**
     * @return set of key-to-action bindings
     */
    Set<ActionBinding> actionBindings();

    /**
     * @return assets (fonts, sounds, colors, localized texts etc.) for the different games
     */
    GlobalAssets assets();

    /**
     * @return watchdog process observing the directory where user-defined maps are stored
     */
    DirectoryWatchdog directoryWatchdog();

    /**
     * @return the clock driving the game
     */
    GameClock clock();

    /**
     * @return Context giving access to important entities like game controller, game state etc.
     */
    GameContext gameContext();

    /**
     * @return key emulation of NES joypad
     */
    Joypad joypad();

    /**
     * @return global keyboard state
     */
    Keyboard keyboard();

    /**
     * @return sound manager for the current game
     */
    SoundManager soundManager();

    /**
     * @return the primary stage provided by the JavaFX application
     */
    Stage stage();

    /**
     * @return the UI preferences (stored permanently in platform-specific way)
     */
    UIPreferences preferences();

    // Messages

    /** Default duration a flash message appears on the screen. */
    Duration DEFAULT_FLASH_MESSAGE_DURATION = Duration.seconds(1.5);

    /**
     * Shows a message on the screen that slowly fades out.
     *
     * @param duration display duration before fading out
     * @param message the message text
     * @param args arguments merged into the message text using String.format()
     */
    void showFlashMessage(Duration duration, String message, Object... args);

    /**
     * Shows a message on the screen that slowly fades out and displays for a default duration.
     *
     * @param message the message text
     * @param args arguments merged into the message text using String.format()
     */
    default void showFlashMessage(String message, Object... args) {
        showFlashMessage(DEFAULT_FLASH_MESSAGE_DURATION, message, args);
    }

}