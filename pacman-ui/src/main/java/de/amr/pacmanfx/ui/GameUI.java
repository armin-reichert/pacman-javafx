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
import de.amr.pacmanfx.ui.layout.PlayView;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.ui.sound.VoicePlayer;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.PreferencesManager;
import de.amr.pacmanfx.uilib.assets.Translator;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.util.Set;

import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_AUTOPILOT;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_IMMUNITY;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;

/**
 * Central interface for the game UI. Provides access to global state, and lifecycle methods.
 * Implementations manage the JavaFX stage, views, sound, preferences, and configuration.
 */
public interface GameUI extends Translator {

    // -----------------------------------------------------------------------------------------------------------------
    // Key Bindings
    // -----------------------------------------------------------------------------------------------------------------

    /** Global keyboard handler used for all key bindings. */
    Keyboard KEYBOARD = new Keyboard();

    /** Cheat key bindings (Alt + key). */
    Set<ActionBinding> CHEAT_BINDINGS = Set.of(
        new ActionBinding(CheatActions.ACTION_EAT_ALL_PELLETS,  alt(KeyCode.E)),
        new ActionBinding(CheatActions.ACTION_ADD_LIVES,        alt(KeyCode.L)),
        new ActionBinding(CheatActions.ACTION_ENTER_NEXT_LEVEL, alt(KeyCode.N)),
        new ActionBinding(CheatActions.ACTION_KILL_GHOSTS,      alt(KeyCode.X))
    );

    /** Steering key bindings (arrow keys, with optional Ctrl modifier). */
    Set<ActionBinding> STEERING_BINDINGS = Set.of(
        new ActionBinding(ACTION_STEER_UP,    bare(KeyCode.UP),    control(KeyCode.UP)),
        new ActionBinding(ACTION_STEER_DOWN,  bare(KeyCode.DOWN),  control(KeyCode.DOWN)),
        new ActionBinding(ACTION_STEER_LEFT,  bare(KeyCode.LEFT),  control(KeyCode.LEFT)),
        new ActionBinding(ACTION_STEER_RIGHT, bare(KeyCode.RIGHT), control(KeyCode.RIGHT))
    );

    /** Key bindings for scene/level tests. */
    Set<ActionBinding> SCENE_TESTS_BINDINGS = Set.of(
        new ActionBinding(TestActions.ACTION_CUT_SCENES_TEST,      alt(KeyCode.C)),
        new ActionBinding(TestActions.ACTION_SHORT_LEVEL_TEST,     alt(KeyCode.T)),
        new ActionBinding(TestActions.ACTION_MEDIUM_LEVEL_TEST,    alt_shift(KeyCode.T))
    );

    /** Key bindings specific to 3D play scene navigation and rendering. */
    Set<ActionBinding> PLAY_3D_BINDINGS = Set.of(
        new ActionBinding(ACTION_PERSPECTIVE_PREVIOUS,             alt(KeyCode.LEFT)),
        new ActionBinding(ACTION_PERSPECTIVE_NEXT,                 alt(KeyCode.RIGHT)),
        new ActionBinding(ACTION_TOGGLE_DRAW_MODE,                 alt(KeyCode.W))
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

    // -----------------------------------------------------------------------------------------------------------------
    // Global Properties
    // -----------------------------------------------------------------------------------------------------------------

    /** Global property for canvas background color. */
    ObjectProperty<Color> PROPERTY_CANVAS_BACKGROUND_COLOR = new SimpleObjectProperty<>(Color.BLACK);

    /** Global property controlling canvas font smoothing. */
    BooleanProperty PROPERTY_CANVAS_FONT_SMOOTHING = new SimpleBooleanProperty(false);

    /** Global property controlling visibility of debug information. */
    BooleanProperty PROPERTY_DEBUG_INFO_VISIBLE = new SimpleBooleanProperty(false);

    /** Global property for mini-view height. */
    IntegerProperty PROPERTY_MINI_VIEW_HEIGHT = new SimpleIntegerProperty(400);

    /** Global property controlling mini-view visibility. */
    BooleanProperty PROPERTY_MINI_VIEW_ON = new SimpleBooleanProperty(false);

    /** Global property for mini-view opacity (0-100). */
    IntegerProperty PROPERTY_MINI_VIEW_OPACITY_PERCENT = new SimpleIntegerProperty(69);

    /** Global property controlling audio muting. */
    BooleanProperty PROPERTY_MUTED = new SimpleBooleanProperty(false);

    /** Global property for number of simulation steps per tick. */
    IntegerProperty PROPERTY_SIMULATION_STEPS = new SimpleIntegerProperty(1);

    /** Global property controlling visibility of 3D axes. */
    BooleanProperty PROPERTY_3D_AXES_VISIBLE = new SimpleBooleanProperty(false);

    /** Global property for 3D draw mode (fill/line). */
    ObjectProperty<DrawMode> PROPERTY_3D_DRAW_MODE = new SimpleObjectProperty<>(DrawMode.FILL);

    /** Global property enabling/disabling 3D rendering. */
    BooleanProperty PROPERTY_3D_ENABLED = new SimpleBooleanProperty(false);

    /** Global property for 3D floor color. */
    ObjectProperty<Color> PROPERTY_3D_FLOOR_COLOR = new SimpleObjectProperty<>(Color.rgb(20,20,20));

    /** Global property for 3D light color. */
    ObjectProperty<Color> PROPERTY_3D_LIGHT_COLOR = new SimpleObjectProperty<>(Color.WHITE);

    /** Global property for current 3D perspective. */
    ObjectProperty<PerspectiveID> PROPERTY_3D_PERSPECTIVE_ID = new SimpleObjectProperty<>(PerspectiveID.TRACK_PLAYER);

    /** Global property for 3D wall height. */
    DoubleProperty PROPERTY_3D_WALL_HEIGHT = new SimpleDoubleProperty();

    /** Global property for 3D wall opacity (0.0-1.0). */
    DoubleProperty PROPERTY_3D_WALL_OPACITY = new SimpleDoubleProperty(1.0);

    // -----------------------------------------------------------------------------------------------------------------
    // Accessors
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * @return watchdog process observing the directory where user-defined maps are stored
     */
    DirectoryWatchdog customDirWatchdog();

    /**
     * @return the clock driving the game simulation and rendering
     */
    GameClock clock();

    /**
     * @return the non-UI game context (model, variants, etc.)
     */
    GameContext context();

    /**
     * @return the primary JavaFX stage
     */
    Stage stage();

    /**
     * @return the sound manager for playing game sounds
     */
    SoundManager soundManager();

    /**
     * @return the preferences manager for UI settings
     */
    PreferencesManager prefs();

    /**
     * @return voice player for sequential voice playback (one at a time)
     */
    VoicePlayer voicePlayer();

    // -----------------------------------------------------------------------------------------------------------------
    // Messages
    // -----------------------------------------------------------------------------------------------------------------

    /** Default duration for flash messages. */
    Duration DEFAULT_FLASH_MESSAGE_DURATION = Duration.seconds(1.5);

    /**
     * Shows a fading flash message on screen.
     *
     * @param duration display duration before fading
     * @param message  message text (supports {@link String#format(String, Object...)})
     * @param args     formatting arguments
     */
    void showFlashMessage(Duration duration, String message, Object... args);

    /**
     * Shows a fading flash message with default duration.
     *
     * @param message message text
     * @param args    formatting arguments
     */
    default void showFlashMessage(String message, Object... args) {
        showFlashMessage(DEFAULT_FLASH_MESSAGE_DURATION, message, args);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Scene access
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * @return configuration of the current game scene
     */
    GameSceneConfig currentGameSceneConfig();

    /**
     * @param sceneID scene identifier
     * @return true if the current game scene matches the given ID
     */
    boolean currentGameSceneHasID(GameSceneConfig.SceneID sceneID);

    // -----------------------------------------------------------------------------------------------------------------
    // View access
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * @return manager for all UI views (start pages, play view, editor, etc.)
     */
    ViewManager views();

    /** Switches to the editor view (if allowed). */
    void showEditorView();

    /** Switches to the play view. */
    void showPlayView();

    /** Switches to the start pages view. */
    void showStartView();

    default PlayView playView() {
        return views().getView(ViewManager.ViewID.PLAY_VIEW, PlayView.class);
    }

    default Dashboard dashboard() {
        return playView().dashboard();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Config
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * @return manager for UI configurations per game variant
     */
    UIConfigManager uiConfigManager();

    /**
     * @param gameVariantName name of the game variant
     * @return UI configuration for the specified variant
     */
    UIConfig config(String gameVariantName);

    /**
     * @return current UI configuration (cast to expected type)
     * @param <T> expected configuration type
     */
    <T extends UIConfig> T currentConfig();

    // -----------------------------------------------------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------------------------------------------------

    /** Stops the current game (clock, sounds, scene). */
    void stopGame();

    /**
     * Quits the current game scene (if any) and returns to the start page.
     */
    void quitCurrentGameScene();

    /**
     * Resets clock speed and shows the boot screen for the selected game.
     */
    void restart();

    /**
     * Shows the UI (centered) and displays the first start page.
     */
    void show();

    /**
     * Terminates the UI, stops clock, and cleans up resources.
     */
    void terminate();

    /**
     * Opens the given world map file in the editor view.
     *
     * @param worldMapFile world map file to edit
     */
    void openWorldMapFileInEditor(File worldMapFile);
}