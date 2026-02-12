/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.DirectoryWatchdog;
import de.amr.pacmanfx.ui._3d.PerspectiveID;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.action.TestActions;
import de.amr.pacmanfx.ui.dashboard.Dashboard;
import de.amr.pacmanfx.ui.input.Keyboard;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.sound.VoiceManager;
import de.amr.pacmanfx.GameClock;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.assets.Translator;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Optional;
import java.util.Set;

import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_AUTOPILOT;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_IMMUNITY;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;

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
 *   <li>Provide global UI properties (colors, debug flags, 3D settings, etc.)</li>
 *   <li>Handle keyboard bindings and dispatch actions</li>
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
    // Key Bindings
    // ---------------------------------------------------------------------------------------------

    /**
     * Global keyboard handler used for all key bindings.
     * <p>
     * Implementations typically register this handler with the JavaFX scene.
     */
    Keyboard KEYBOARD = new Keyboard();

    /** Cheat key bindings (Alt + key). */
    Set<ActionBinding> CHEAT_BINDINGS = Set.of(
        new ActionBinding(CheatActions.ACTION_EAT_ALL_PELLETS,  alt(KeyCode.E)),
        new ActionBinding(CheatActions.ACTION_ADD_LIVES,        alt(KeyCode.L)),
        new ActionBinding(CheatActions.ACTION_ENTER_NEXT_LEVEL, alt(KeyCode.N)),
        new ActionBinding(CheatActions.ACTION_KILL_GHOSTS,      alt(KeyCode.X))
    );

    /** Steering key bindings (arrow keys, optionally with Ctrl). */
    Set<ActionBinding> STEERING_BINDINGS = Set.of(
        new ActionBinding(ACTION_STEER_UP,    bare(KeyCode.UP),    control(KeyCode.UP)),
        new ActionBinding(ACTION_STEER_DOWN,  bare(KeyCode.DOWN),  control(KeyCode.DOWN)),
        new ActionBinding(ACTION_STEER_LEFT,  bare(KeyCode.LEFT),  control(KeyCode.LEFT)),
        new ActionBinding(ACTION_STEER_RIGHT, bare(KeyCode.RIGHT), control(KeyCode.RIGHT))
    );

    /** Key bindings for scene/level test utilities. */
    Set<ActionBinding> SCENE_TESTS_BINDINGS = Set.of(
        new ActionBinding(TestActions.ACTION_CUT_SCENES_TEST,      alt(KeyCode.C)),
        new ActionBinding(TestActions.ACTION_SHORT_LEVEL_TEST,     alt(KeyCode.T)),
        new ActionBinding(TestActions.ACTION_MEDIUM_LEVEL_TEST,    alt_shift(KeyCode.T))
    );

    /** Common global key bindings used across all views/scenes. */
    Set<ActionBinding> COMMON_BINDINGS = Set.of(
        new ActionBinding(ACTION_BOOT_SHOW_PLAY_VIEW,              bare(KeyCode.F3)),
        new ActionBinding(ACTION_ENTER_FULLSCREEN,                 bare(KeyCode.F11)),
        new ActionBinding(ACTION_OPEN_EDITOR,                      alt_shift(KeyCode.E)),
        new ActionBinding(ACTION_SHOW_HELP,                        bare(KeyCode.H)),
        new ActionBinding(ACTION_QUIT_GAME_SCENE,                  bare(KeyCode.Q)),
        new ActionBinding(ACTION_SIMULATION_SLOWER,                alt(KeyCode.MINUS)),
        new ActionBinding(ACTION_SIMULATION_SLOWEST,               alt_shift(KeyCode.MINUS)),
        new ActionBinding(ACTION_SIMULATION_FASTER,                alt(KeyCode.PLUS)),
        new ActionBinding(ACTION_SIMULATION_FASTEST,               alt_shift(KeyCode.PLUS)),
        new ActionBinding(ACTION_SIMULATION_RESET,                 alt(KeyCode.DIGIT0)),
        new ActionBinding(ACTION_SIMULATION_ONE_STEP,              shift(KeyCode.P), shift(KeyCode.F5)),
        new ActionBinding(ACTION_SIMULATION_TEN_STEPS,             shift(KeyCode.SPACE)),
        new ActionBinding(ACTION_TOGGLE_AUTOPILOT,                 alt(KeyCode.A)),
        new ActionBinding(ACTION_TOGGLE_COLLISION_STRATEGY,        alt(KeyCode.S)),
        new ActionBinding(ACTION_TOGGLE_DEBUG_INFO,                alt(KeyCode.D)),
        new ActionBinding(ACTION_TOGGLE_MUTED,                     alt(KeyCode.M)),
        new ActionBinding(ACTION_TOGGLE_PAUSED,                    bare(KeyCode.P), bare(KeyCode.F5)),
        new ActionBinding(ACTION_TOGGLE_DASHBOARD,                 bare(KeyCode.F1), alt(KeyCode.B)),
        new ActionBinding(ACTION_TOGGLE_IMMUNITY,                  alt(KeyCode.I)),
        new ActionBinding(ACTION_TOGGLE_MINI_VIEW_VISIBILITY,      bare(KeyCode.F2)),
        new ActionBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D,          alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3))
    );

    // ---------------------------------------------------------------------------------------------
    // Global Properties
    // ---------------------------------------------------------------------------------------------

    /**
     * Global property for the canvas background color.
     * <p>
     * Implementations should bind this to the rendering surface.
     */
    ObjectProperty<Color> PROPERTY_CANVAS_BACKGROUND_COLOR = new SimpleObjectProperty<>(Color.BLACK);

    /** Whether canvas font smoothing is enabled. */
    BooleanProperty PROPERTY_CANVAS_FONT_SMOOTHING = new SimpleBooleanProperty(false);

    /** Whether debug information overlays are visible. */
    BooleanProperty PROPERTY_DEBUG_INFO_VISIBLE = new SimpleBooleanProperty(false);

    /** Height of the mini-view (in pixels). */
    IntegerProperty PROPERTY_MINI_VIEW_HEIGHT = new SimpleIntegerProperty(400);

    /** Whether the mini-view is currently visible. */
    BooleanProperty PROPERTY_MINI_VIEW_ON = new SimpleBooleanProperty(false);

    /** Opacity of the mini-view (0–100%). */
    IntegerProperty PROPERTY_MINI_VIEW_OPACITY_PERCENT = new SimpleIntegerProperty(69);

    /** Whether all audio output is muted. */
    BooleanProperty PROPERTY_MUTED = new SimpleBooleanProperty(false);

    /** Number of simulation steps executed per clock tick. */
    IntegerProperty PROPERTY_SIMULATION_STEPS = new SimpleIntegerProperty(1);

    /** Whether 3D axes are visible in the 3D play scene. */
    BooleanProperty PROPERTY_3D_AXES_VISIBLE = new SimpleBooleanProperty(false);

    /** Draw mode for 3D geometry (fill or wireframe). */
    ObjectProperty<DrawMode> PROPERTY_3D_DRAW_MODE = new SimpleObjectProperty<>(DrawMode.FILL);

    /** Whether 3D rendering is enabled at all. */
    BooleanProperty PROPERTY_3D_ENABLED = new SimpleBooleanProperty(false);

    /** Floor color used in 3D mode. */
    ObjectProperty<Color> PROPERTY_3D_FLOOR_COLOR = new SimpleObjectProperty<>(Color.rgb(20, 20, 20));

    /** Light color used in 3D mode. */
    ObjectProperty<Color> PROPERTY_3D_LIGHT_COLOR = new SimpleObjectProperty<>(Color.WHITE);

    /** Currently active 3D camera perspective. */
    ObjectProperty<PerspectiveID> PROPERTY_3D_PERSPECTIVE_ID = new SimpleObjectProperty<>(PerspectiveID.TRACK_PLAYER);

    /** Height of 3D walls (in world units). */
    DoubleProperty PROPERTY_3D_WALL_HEIGHT = new SimpleDoubleProperty();

    /** Opacity of 3D walls (0.0–1.0). */
    DoubleProperty PROPERTY_3D_WALL_OPACITY = new SimpleDoubleProperty(1.0);

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

    /** Default duration for flash messages. */
    Duration DEFAULT_FLASH_MESSAGE_DURATION = Duration.seconds(1.5);

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
        showFlashMessage(DEFAULT_FLASH_MESSAGE_DURATION, message, args);
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

    /**
     * Returns the current game scene if existing.
     * @return (optional) game scene
     */
    default Optional<GameScene> optGameScene() {
        return views().getPlayView().optGameScene();
    }

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
    default Dashboard dashboard() {
        return views().getPlayView().dashboard();
    }

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
