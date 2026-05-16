/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.action.TestActions;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.util.Duration;

import java.util.Set;

import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_AUTOPILOT;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_IMMUNITY;
import static de.amr.pacmanfx.ui.action.CommonGameActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;

public class GameUIConstants {

    private GameUIConstants() {}

    /** Cheat key bindings (Alt + key). */
    public static final Set<ActionBinding> CHEAT_ACTION_BINDINGS = Set.of(
        new ActionBinding(CheatActions.ACTION_EAT_ALL_PELLETS,  alt(KeyCode.E)),
        new ActionBinding(CheatActions.ACTION_ADD_LIVES,        alt(KeyCode.L)),
        new ActionBinding(CheatActions.ACTION_ENTER_NEXT_LEVEL, alt(KeyCode.N)),
        new ActionBinding(CheatActions.ACTION_KILL_GHOSTS,      alt(KeyCode.X))
    );
    /** Steering key bindings (arrow keys, optionally with Ctrl). */
    public static final Set<ActionBinding> STEERING_ACTION_BINDINGS = Set.of(
        new ActionBinding(ACTION_STEER_UP,    bare(KeyCode.UP),    control(KeyCode.UP)),
        new ActionBinding(ACTION_STEER_DOWN,  bare(KeyCode.DOWN),  control(KeyCode.DOWN)),
        new ActionBinding(ACTION_STEER_LEFT,  bare(KeyCode.LEFT),  control(KeyCode.LEFT)),
        new ActionBinding(ACTION_STEER_RIGHT, bare(KeyCode.RIGHT), control(KeyCode.RIGHT))
    );
    /** Key bindings for scene/level test utilities. */
    public static final Set<ActionBinding> SCENE_TESTS_BINDINGS = Set.of(
        new ActionBinding(TestActions.ACTION_CUT_SCENES_TEST,      alt(KeyCode.C)),
        new ActionBinding(TestActions.ACTION_SHORT_LEVEL_TEST,     alt(KeyCode.T)),
        new ActionBinding(TestActions.ACTION_MEDIUM_LEVEL_TEST,    alt_shift(KeyCode.T))
    );
    /** Common global key bindings used across all views/scenes. */
    public static final Set<ActionBinding> COMMON_BINDINGS = Set.of(
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
        new ActionBinding(ACTION_TOGGLE_KEYBOARD_MONITOR,          alt(KeyCode.K)),
        new ActionBinding(ACTION_TOGGLE_MUTED,                     alt(KeyCode.M)),
        new ActionBinding(ACTION_TOGGLE_PAUSED,                    bare(KeyCode.P), bare(KeyCode.F5)),
        new ActionBinding(ACTION_TOGGLE_DASHBOARD,                 bare(KeyCode.F1), alt(KeyCode.B)),
        new ActionBinding(ACTION_TOGGLE_IMMUNITY,                  alt(KeyCode.I)),
        new ActionBinding(ACTION_TOGGLE_MINI_VIEW_VISIBILITY,      bare(KeyCode.F2)),
        new ActionBinding(ACTION_TOGGLE_PLAY_SCENE_2D_3D,          alt(KeyCode.DIGIT3), alt(KeyCode.NUMPAD3))
    );
    /**
     * Global property for the canvas background color.
     * <p>
     * Implementations should bind this to the rendering surface.
     */
    public static final ObjectProperty<Color> PROPERTY_CANVAS_BACKGROUND_COLOR = new SimpleObjectProperty<>(Color.BLACK);
    /** Whether canvas font smoothing is enabled. */
    public static final BooleanProperty PROPERTY_CANVAS_FONT_SMOOTHING = new SimpleBooleanProperty(false);
    /** Whether debug information overlays are visible. */
    public static final BooleanProperty PROPERTY_DEBUG_INFO_VISIBLE = new SimpleBooleanProperty(false);
    /** Whether information about the currently pressed keys is displayed. */
    public static final BooleanProperty PROPERTY_KEYBOARD_MONITOR_VISIBLE = new SimpleBooleanProperty(false);
    /** Height of the mini-view (in pixels). */
    public static final IntegerProperty PROPERTY_MINI_VIEW_HEIGHT = new SimpleIntegerProperty(400);
    /** Whether the mini-view is currently visible. */
    public static final BooleanProperty PROPERTY_MINI_VIEW_ON = new SimpleBooleanProperty(false);
    /** Opacity of the mini-view (0–100%). */
    public static final IntegerProperty PROPERTY_MINI_VIEW_OPACITY_PERCENT = new SimpleIntegerProperty(69);
    /** Whether all audio output is muted. */
    public static final BooleanProperty PROPERTY_MUTED = new SimpleBooleanProperty(false);
    /** Number of simulation steps executed per clock tick. */
    public static final IntegerProperty PROPERTY_SIMULATION_STEPS = new SimpleIntegerProperty(1);
    /** Whether 3D axes are visible in the 3D play scene. */
    public static final BooleanProperty PROPERTY_3D_AXES_VISIBLE = new SimpleBooleanProperty(false);
    /** Draw mode for 3D geometry (fill or wireframe). */
    public static final ObjectProperty<DrawMode> PROPERTY_3D_DRAW_MODE = new SimpleObjectProperty<>(DrawMode.FILL);
    /** Whether 3D rendering is enabled at all. */
    public static final BooleanProperty PROPERTY_3D_ENABLED = new SimpleBooleanProperty(false);
    /** Floor color used in 3D mode. */
    public static final ObjectProperty<Color> PROPERTY_3D_FLOOR_COLOR = new SimpleObjectProperty<>(Color.rgb(20, 20, 20));
    /** Light color used in 3D mode. */
    public static final ObjectProperty<Color> PROPERTY_3D_LIGHT_COLOR = new SimpleObjectProperty<>(Color.WHITE);
    /** Currently active 3D camera perspective. */
    public static final ObjectProperty<PerspectiveID> PROPERTY_3D_PERSPECTIVE_ID = new SimpleObjectProperty<>(PerspectiveID.TRACK_PLAYER);
    /** Height of 3D walls (in world units). */
    public static final DoubleProperty PROPERTY_3D_WALL_HEIGHT = new SimpleDoubleProperty();
    /** Opacity of 3D walls (0.0–1.0). */
    public static final DoubleProperty PROPERTY_3D_WALL_OPACITY = new SimpleDoubleProperty(1.0);
    /** Default duration for flash messages. */
    public static final Duration DEFAULT_FLASH_MESSAGE_DURATION = Duration.seconds(1.5);
}
