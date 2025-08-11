/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public interface GameUI extends GameLifecycle, GameViewAccess, GameSceneAccess {

    /**
     * @return list of key-to-action bindings
     */
    List<ActionBinding> actionBindings();

    PacManGames_Assets assets();
    DirectoryWatchdog directoryWatchdog();
    GameClock clock();
    GameContext gameContext();
    Joypad joypad();
    Keyboard keyboard();
    SoundManager sound();
    Stage stage();
    UIPreferences uiPreferences();

    /**
     * @param gameVariant name of game variant
     * @return UI configuration for given game variant
     */
    GameUI_Config config(String gameVariant);

    /**
     * Stores the UI configuration for a game variant and initializes the game scenes (assigns the game context).
     *
     * @param variant a game variant
     * @param config the UI configuration for this variant
     */
    void setConfig(String variant, GameUI_Config config);

    /**
     * @return UI configuration for the current game
     * @param <T> type of UI configuration
     */
    <T extends GameUI_Config> T currentConfig();

    Duration DEFAULT_FLASH_MESSAGE_DURATION = Duration.seconds(1.5);

    void showFlashMessageSec(Duration duration, String message, Object... args);

    default void showFlashMessage(String message, Object... args) {
        showFlashMessageSec(DEFAULT_FLASH_MESSAGE_DURATION, message, args);
    }
}