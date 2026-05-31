/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.layout.StatusIconBox;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

/**
 * Central interface for the Pac-Man FX user interface layer.
 */
public interface GameUI {

    /**
     * @return  the primary JavaFX stage.
     */
    Stage stage();

    /**
     * @return the mains scene of the game UI
     */
    Scene scene();

    StatusIconBox statusIconBox();

    // ---------------------------------------------------------------------------------------------
    // Service facade (game context, views, game scenes, flash messages, sound etc.)
    // ---------------------------------------------------------------------------------------------

    GameUI_ServiceFacade facade();

    // ---------------------------------------------------------------------------------------------
    // Lifecycle
    // ---------------------------------------------------------------------------------------------

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
