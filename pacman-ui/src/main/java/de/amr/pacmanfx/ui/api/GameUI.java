/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui.api;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.ui.*;
import de.amr.pacmanfx.ui.input.Joypad;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.UIPreferences;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public interface GameUI extends GameUI_Lifecycle, GameUI_ConfigManager, GameUI_ViewAccess, GameUI_SceneAccess {

    /**
     * @return list of key-to-action bindings
     */
    List<ActionBinding> actionBindings();

    GameAssets assets();

    DirectoryWatchdog directoryWatchdog();

    GameClock clock();

    GameContext gameContext();

    Joypad joypad();

    Keyboard keyboard();

    SoundManager sound();

    Stage stage();

    UIPreferences uiPreferences();

    /**
     * Shows a message on the screen that slowly fades out.
     *
     * @param duration display duration before fading out
     * @param message the message text
     * @param args arguments merged into the message text using String.format()
     */
    void showFlashMessage(Duration duration, String message, Object... args);

    /**
     * Shows a message on the screen that slowly fades out.
     *
     * @param message the message text
     * @param args arguments merged into the message text using String.format()
     */
    void showFlashMessage(String message, Object... args);
}