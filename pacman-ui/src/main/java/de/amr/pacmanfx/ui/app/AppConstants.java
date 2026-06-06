/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.app;

import de.amr.basics.math.Vector3f;
import de.amr.pacmanfx.ui.action.ActionBinding;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.action.TestActions;
import de.amr.pacmanfx.ui.d3.animation.energizer.AttractionConfig;
import de.amr.pacmanfx.ui.d3.animation.energizer.ExplosionConfig;
import de.amr.pacmanfx.ui.d3.animation.energizer.ParticlesAnimationConfig;
import de.amr.pacmanfx.ui.d3.animation.energizer.SwirlConfig;
import de.amr.pacmanfx.ui.d3.camera.PerspectiveID;
import de.amr.pacmanfx.ui.subviews.dashboard.DashboardConfig;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.rendering.Gradients;
import javafx.beans.property.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Background;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.File;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.regex.Pattern;

import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_AUTOPILOT;
import static de.amr.pacmanfx.ui.action.CheatActions.ACTION_TOGGLE_IMMUNITY;
import static de.amr.pacmanfx.ui.action.CommonActions.*;
import static de.amr.pacmanfx.ui.input.Keyboard.*;
import static de.amr.pacmanfx.uilib.UfxBackgrounds.createImageBackground;

public class AppConstants {

    private AppConstants() {}

    /**
     * Game variant names must match this pattern (e.g. "MS_PACMAN_2024").
     */
    public static final Pattern GAME_VARIANT_NAME_PATTERN = Pattern.compile("[A-Z][A-Z_0-9]*");

    /**
     * Directory under which the user specific files are stored.
     * <p>Default: <code>$HOME/.pacmanfx</code> (Unix) or <code>%USERPROFILE%\.pacmanfx</code> (MS Windows)</p>
     */
    public static final File USER_HOME_DIR = new File(System.getProperty("user.home"), ".pacmanfx");

    /**
     * Directory where custom maps are stored (default: <code>&lt;home_dir&gt;/maps</code>).
     */
    public static final File CUSTOM_MAP_DIR = new File(USER_HOME_DIR, "maps");


    public static final int MIN_STAGE_WIDTH  = 280;

    public static final int MIN_STAGE_HEIGHT = 360;

    /**
     * Resource manager for UI assets (fonts, images, sounds).
     */
    public static final ResourceManager UI_RESOURCES = () -> AppContext.class;

    /**
     * Voice media for immunity activation.
     */
    public static final Media VOICE_IMMUNITY_ON = UI_RESOURCES.loadMedia("/de/amr/pacmanfx/ui/sound/voice/immunity-on.mp3");

    /**
     * Voice media for immunity deactivation.
     */
    public static final Media VOICE_IMMUNITY_OFF = UI_RESOURCES.loadMedia("/de/amr/pacmanfx/ui/sound/voice/immunity-off.mp3");

    /**
     * Voice media for game start explanation.
     */
    public static final Media VOICE_EXPLAIN_GAME_START = UI_RESOURCES.loadMedia("/de/amr/pacmanfx/ui/sound/voice/press-key.mp3");

    /**
     * Voice media for autopilot activation.
     */
    public static final Media VOICE_AUTOPILOT_ON = UI_RESOURCES.loadMedia("/de/amr/pacmanfx/ui/sound/voice/autopilot-on.mp3");

    /**
     * Voice media for autopilot deactivation.
     */
    public static final Media VOICE_AUTOPILOT_OFF = UI_RESOURCES.loadMedia("/de/amr/pacmanfx/ui/sound/voice/autopilot-off.mp3");

    /**
     * Good Pac-Man font (alternative variant).
     */
    public static final Font FONT_PAC_FONT_GOOD = UI_RESOURCES.loadFont("/de/amr/pacmanfx/ui/fonts/PacfontGood.ttf", 8);

    /**
     * Standard Pac-Man font.
     */
    public static final Font FONT_PAC_FONT = UI_RESOURCES.loadFont("/de/amr/pacmanfx/ui/fonts/Pacfont.ttf", 8);

    /**
     * Condensed monospace font for UI elements.
     */
    public static final Font FONT_CONDENSED = UI_RESOURCES.loadFont("/de/amr/pacmanfx/ui/fonts/Inconsolata_Condensed-Bold.ttf", 12);

    /**
     * Monospaced font for debug/info text.
     */
    public static final Font FONT_MONOSPACED = UI_RESOURCES.loadFont("/de/amr/pacmanfx/ui/fonts/fantasquesansmono-bold.otf", 12);

    /**
     * Handwriting-style font for messages.
     */
    public static final Font FONT_HANDWRITING = UI_RESOURCES.loadFont("/de/amr/pacmanfx/ui/fonts/Molle-Italic.ttf", 9);

    /**
     * Arcade font size 8.
     */
    public static final Font FONT_ARCADE_8 = UI_RESOURCES.loadFont("/de/amr/pacmanfx/ui/fonts/emulogic.ttf", 8);

    /**
     * Arcade font size 6.
     */
    public static final Font FONT_ARCADE_6 = UI_RESOURCES.loadFont("/de/amr/pacmanfx/ui/fonts/emulogic.ttf", 6);

    /**
     * Background image using Pac-Man wallpaper.
     */
    public static final Background BACKGROUND_PAC_MAN_WALLPAPER = createImageBackground(UI_RESOURCES.loadImage("/de/amr/pacmanfx/ui/graphics/pacman_wallpaper.png"));

    /**
     * Localized text bundle for the UI.
     */
    public static final ResourceBundle LOCALIZED_TEXTS = UI_RESOURCES.getModuleBundle("de.amr.pacmanfx.ui.localized_texts");

    /**
     * Path to the main UI stylesheet.
     */
    public static final String STYLE_SHEET_PATH = "/de/amr/pacmanfx/ui/css/style.css";

    public static final Color CONTEXT_MENU_DEFAULT_TITLE_COLOR = Color.CORNFLOWERBLUE;

    public static final Font CONTEXT_MENU_DEFAULT_TITLE_FONT = Font.font("Dialog", FontWeight.BLACK, 14.0f);

    public static final DashboardConfig DEFAULT_DASHBOARD_CONFIG = new DashboardConfig(
        110, // label width
        320, // width
        Color.rgb(0, 0, 50, 1.0), // background
        Color.WHITE, // text
        Font.font("Sans", 12), // label font
        Font.font("Sans", 12) // content font
    );

    public static final Background[] WALLPAPERS = Arrays.stream(Gradients.Samples.values())
        .map(Gradients.Samples::gradient)
        .map(Background::fill)
        .toArray(Background[]::new);

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

    public static final ParticlesAnimationConfig DEFAULT_PARTICLE_ANIMATION_CONFIG = new ParticlesAnimationConfig(
        new ExplosionConfig(
            new Vector3f(0, 0, 0.1f), // gravity
            500,        // num particles by explosion
            0.25f,      // mean particle radius
            0.1f, 0.4f, // min/max particle speed horizontally (xy-plane)
            1.5f, 6     // min/max particle speed horizontally (z-direction)
        ),
        new AttractionConfig(0.004f, 0.4f, 0.3f, 0.5f),
        new SwirlConfig(4, 20, 0.3f, 0.05f)
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
