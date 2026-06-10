/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.game;

import de.amr.pacmanfx.ui.subviews.dashboard.DashboardConfig;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.rendering.Gradients;
import javafx.beans.property.*;
import javafx.scene.layout.Background;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.io.File;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static de.amr.pacmanfx.uilib.UfxBackgrounds.createImageBackground;

public class GameConstants {

    private GameConstants() {}

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

    // Simulation speed changes

    public static float SIM_STEP_MESSAGE_SEC = 0.75f;

    public static final int SIM_SPEED_DELTA = 2;

    public static final int SIM_SPEED_MIN = 5;

    public static final int SIM_SPEED_MAX = 300;

    /**
     * Resource manager for UI assets (fonts, images, sounds).
     */
    public static final ResourceManager UI_RESOURCES = () -> GameConstants.class;

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

    /** Default duration for flash messages. */
    public static final Duration DEFAULT_FLASH_MESSAGE_DURATION = Duration.seconds(1.5);
}
