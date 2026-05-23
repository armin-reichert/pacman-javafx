/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.filesystem.DirectoryWatchdog;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.layout.MiniGameView;
import de.amr.pacmanfx.ui.layout.ViewManager;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.sound.VoiceManager;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.assets.Translator;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Optional;

/**
 * Central interface for the Pac-Man FX user interface layer.
 * <p>
 * A {@code GameUI} implementation owns and orchestrates all JavaFX-facing
 * components: the primary stage, view management, sound, preferences,
 * configuration, and global UI state. It also exposes lifecycle hooks used by
 * the game engine to start, stop, and transition between scenes.
 * <p>
 * The interface is intentionally broad: it acts as the façade through which
 * the non-UI game logic interacts with the presentation layer.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Manage the JavaFX stage and all active views</li>
 *   <li>Coordinate sound, voice playback, and preferences</li>
 *   <li>Expose lifecycle operations (show, restart, terminate)</li>
 *   <li>Provide access to the current game scene and its configuration</li>
 * </ul>
 *
 * <h2>Threading</h2>
 * All UI-modifying methods must be invoked on the JavaFX Application Thread.
 * Implementations may internally schedule work via {@code Platform.runLater}.
 *
 * <h2>Translation</h2>
 * Extends {@link Translator} so all UI text can be localized.
 */
public interface GameUI extends Translator {

    // ---------------------------------------------------------------------------------------------
    // Accessors
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the watchdog monitoring the directory where user-defined maps are stored.
     * <p>
     * Implementations typically start this watcher during initialization.
     */
    DirectoryWatchdog customDirWatchdog();

    /**
     * Returns the non-UI game context (model, variants, rules, etc.).
     * <p>
     * This is the primary entry point for interacting with the game engine.
     */
    GameContext gameContext();

    /**
     * Returns the primary JavaFX stage.
     * <p>
     * Implementations own and configure this stage.
     */
    Stage stage();

    /**
     * Returns the sound manager responsible for playing sound effects.
     */
    SoundManager soundManager();

    SpriteAnimationSet spriteAnimationSet();

    /**
     * Returns the preferences manager storing UI-related settings.
     */
    PreferencesManager prefs();

    /**
     * Returns the voice player used for sequential voice playback.
     * <p>
     * Only one voice clip plays at a time.
     */
    VoiceManager voicePlayer();

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

    /**
     * Returns the configuration of the currently active game scene.
     * <p>
     * This includes scene type, camera mode, and other scene-specific settings.
     */
    GameSceneConfig currentGameSceneConfig();

    /**
     * Checks whether the current game scene matches the given ID.
     *
     * @param sceneID scene identifier
     * @return {@code true} if the active scene has the given ID
     */
    boolean currentGameSceneHasID(GameSceneConfig.SceneID sceneID);

    void forceGameSceneUpdate();

    /**
     * Returns the current game scene if existing.
     * @return (optional) game scene
     */
    Optional<GameScene> optGameScene();

    // ---------------------------------------------------------------------------------------------
    // View Access
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the manager responsible for all UI views (start pages, play view, editor, etc.).
     */
    ViewManager views();

    /** Switches to the editor view, if allowed by the current game state. */
    void showEditorView();

    /** Switches to the play view. */
    void showPlayView();

    /** Switches to the start pages view. */
    void showStartView();

    /**
     * Convenience accessor for the dashboard inside the play view.
     */
    Dashboard dashboard();

    MiniGameView miniView();
    
    // ---------------------------------------------------------------------------------------------
    // Config
    // ---------------------------------------------------------------------------------------------

    /**
     * Returns the manager for UI configurations per game variant.
     */
    UIConfigManager uiConfigManager();

    /**
     * Returns the UI configuration for the specified game variant.
     *
     * @param gameVariantName name of the variant
     */
    UIConfig config(String gameVariantName);

    /**
     * Returns the current UI configuration, cast to the expected type.
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
