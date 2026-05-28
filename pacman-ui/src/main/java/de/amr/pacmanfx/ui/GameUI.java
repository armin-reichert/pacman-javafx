/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.ui.layout.ViewManager;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;

/**
 * Central interface for the Pac-Man FX user interface layer.
 */
public interface GameUI {

    // ---------------------------------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------------------------------

    /**
     * @return  the watchdog monitoring the directory where user-defined maps are stored.
     * <p>
     * Implementations typically start this watcher during initialization.
     */
    DirectoryWatchdog customDirWatchdog();

    /**
     * @return the non-UI game context (model, variants, rules, etc.).
     * <p>
     * This is the primary entry point for interacting with the game engine.
     */
    GameContext gameContext();

    /**
     * @return  the primary JavaFX stage.
     * <p>
     * Implementations own and configure this stage.
     */
    Stage stage();

    Scene scene();

    /**
     * @return the sound manager responsible for playing sound effects.
     */
    SoundManager soundManager();

    /**
     * @return the set of all sprite animations
     */
    SpriteAnimationSet spriteAnimationSet();

    /**
     * @return the preferences manager storing UI-related settings.
     */
    PreferencesManager preferencesManager();

    /**
     * @return translation service for localized UI messages
     */
    TranslationManager translationManager();

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
    // Scene Access
    // ---------------------------------------------------------------------------------------------

    GameSceneChangeManager gameSceneManager();

    GameSceneEmbeddingManager gameSceneEmbeddingManager();

    /**
     * Checks whether the current game scene matches the given ID.
     *
     * @param sceneID scene identifier
     * @return {@code true} if the active scene has the given ID
     */
    boolean currentGameSceneHasID(GameSceneConfig.SceneID sceneID);

    // ---------------------------------------------------------------------------------------------
    // View Access
    // ---------------------------------------------------------------------------------------------

    /**
     * @return the manager responsible for all UI views (start pages, play view, editor, etc.).
     */
    ViewManager viewManager();

    // ---------------------------------------------------------------------------------------------
    // Config
    // ---------------------------------------------------------------------------------------------

    /**
     * @return the manager for UI configurations per game variant.
     */
    UIConfigManager uiConfigManager();

    /**
     * @return the UI configuration for the specified game variant.
     *
     * @param gameVariantName name of the variant
     */
    UIConfig config(String gameVariantName);

    /**
     * @return the current UI configuration, cast to the expected type.
     *
     * @param <T> expected configuration type
     */
    <T extends UIConfig> T currentConfig();

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
     * Quits the current game scene (if any) and returns to the start page.
     */
    void quitCurrentGameScene();

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
