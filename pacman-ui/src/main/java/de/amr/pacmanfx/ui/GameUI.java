/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.ui.layout.StatusIconBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

/**
 * Central interface for the Pac-Man FX user interface layer.
 */
public interface GameUI {

    /**
     * @return  the watchdog monitoring the directory where user-defined maps are stored.
     */
    DirectoryWatchdog customDirWatchdog();

    /**
     * @return the non-UI game context (model, variants, rules, etc.).
     */
    GameContext gameContext();

    /**
     * @return  the primary JavaFX stage.
     */
    Stage stage();

    /**
     * @return the mains scene of the game UI
     */
    Scene scene();

    /**
     * @return the set of all sprite animations
     */
    SpriteAnimationSet spriteAnimationSet();

    // ---------------------------------------------------------------------------------------------
    // Messages
    // ---------------------------------------------------------------------------------------------

    /**
     * Displays a fading flash message on screen.
     *
     * @param duration how long the message remains visible before fading
     * @param message  message text (supports {@link String#format})
     * @param args     formatting arguments
     */
    void showFlashMessage(Duration duration, String message, Object... args);

    /**
     * Displays a fading flash message using the default duration.
     *
     * @param message message text
     * @param args    formatting arguments
     */
    default void showFlashMessage(String message, Object... args) {
        showFlashMessage(GameUIConstants.DEFAULT_FLASH_MESSAGE_DURATION, message, args);
    }

    // ---------------------------------------------------------------------------------------------
    // Services (view manager, game scene manager, sound manager etc.)
    // ---------------------------------------------------------------------------------------------

    GameUI_Services services();

    // ---------------------------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------------------------

    StatusIconBox statusIconBox();

    /**
     * Stops the current game, including clock, sounds, and active scene.
     * <p>
     * Implementations must ensure the game can be cleanly restarted afterward.
     */
    void stopGame();

    /**
     * Resets clock speed and shows the boot screen for the selected game.
     * <p>
     * Typically used when switching variants or restarting gameplay.
     */
    void restart();

    /**
     * Shows the UI (centered) and displays the first start page.
     * <p>
     * Called once after application initialization.
     */
    void show();

    /**
     * Terminates the UI, stops the clock, and releases resources.
     * <p>
     * Called when the application is shutting down.
     */
    void terminate();

    /**
     * Opens the given world map file in the editor view.
     *
     * @param worldMapFile world map file to edit
     */
    void openWorldMapFileInEditor(File worldMapFile);
}
