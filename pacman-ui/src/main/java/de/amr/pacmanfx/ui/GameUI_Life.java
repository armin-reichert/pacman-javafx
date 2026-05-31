/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import java.io.File;

/**
 * Game UI lifecycle methods.
 */
public interface GameUI_Life {
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
