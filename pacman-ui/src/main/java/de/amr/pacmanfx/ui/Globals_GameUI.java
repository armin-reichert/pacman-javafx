package de.amr.pacmanfx.ui;

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

import java.util.Arrays;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.uilib.UfxBackgrounds.createImageBackground;

public final class Globals_GameUI {

    public static final int MIN_STAGE_WIDTH  = 280;

    public static final int MIN_STAGE_HEIGHT = 360;

    public static final String RESOURCE_ROOT = "/de/amr/pacmanfx/ui/";

    /**
     * Resource manager for UI assets (fonts, images, sounds).
     */
    public static final ResourceManager RES_MGR = () -> Globals_GameUI.class;

    /**
     * Localized text bundle for the UI.
     */
    public static final ResourceBundle LOCALIZED_TEXTS = RES_MGR.getModuleBundle("de.amr.pacmanfx.ui.localized_texts");

    /**
     * Background image using Pac-Man wallpaper.
     */
    public static final Background BACKGROUND_PAC_MAN_WALLPAPER = createImageBackground(
        RES_MGR.loadImage(RESOURCE_ROOT + "graphics/pacman_wallpaper.png"));

    /**
     * Arcade font size 6.
     */
    public static final Font FONT_ARCADE_6 = RES_MGR.loadFont(RESOURCE_ROOT + "fonts/emulogic.ttf", 6);

    /**
     * Arcade font size 8.
     */
    public static final Font FONT_ARCADE_8 = RES_MGR.loadFont(RESOURCE_ROOT + "fonts/emulogic.ttf", 8);

    /**
     * Handwriting-style font for messages.
     */
    public static final Font FONT_HANDWRITING = RES_MGR.loadFont(RESOURCE_ROOT + "fonts/Molle-Italic.ttf", 9);

    /**
     * Monospaced font for debug/info text.
     */
    public static final Font FONT_MONOSPACED = RES_MGR.loadFont(RESOURCE_ROOT + "fonts/fantasquesansmono-bold.otf", 12);

    /**
     * Condensed monospace font for UI elements.
     */
    public static final Font FONT_CONDENSED = RES_MGR.loadFont(RESOURCE_ROOT + "fonts/Inconsolata_Condensed-Bold.ttf", 12);

    /**
     * Standard Pac-Man font.
     */
    public static final Font FONT_PAC_FONT = RES_MGR.loadFont(RESOURCE_ROOT + "fonts/Pacfont.ttf", 8);

    /**
     * Good Pac-Man font (alternative variant).
     */
    public static final Font FONT_PAC_FONT_GOOD = RES_MGR.loadFont(RESOURCE_ROOT + "fonts/PacfontGood.ttf", 8);

    /**
     * Voice media for autopilot deactivation.
     */
    public static final Media VOICE_AUTOPILOT_OFF = RES_MGR.loadMedia(RESOURCE_ROOT + "sound/voice/autopilot-off.mp3");

    /**
     * Voice media for autopilot activation.
     */
    public static final Media VOICE_AUTOPILOT_ON = RES_MGR.loadMedia(RESOURCE_ROOT + "sound/voice/autopilot-on.mp3");

    /**
     * Voice media for game start explanation.
     */
    public static final Media VOICE_EXPLAIN_GAME_START = RES_MGR.loadMedia(RESOURCE_ROOT + "sound/voice/press-key.mp3");

    /**
     * Voice media for immunity deactivation.
     */
    public static final Media VOICE_IMMUNITY_OFF = RES_MGR.loadMedia(RESOURCE_ROOT + "sound/voice/immunity-off.mp3");

    /**
     * Voice media for immunity activation.
     */
    public static final Media VOICE_IMMUNITY_ON = RES_MGR.loadMedia(RESOURCE_ROOT + "sound/voice/immunity-on.mp3");

    /**
     * Path to the main UI stylesheet.
     */
    public static final String STYLE_SHEET_PATH = RESOURCE_ROOT + "css/style.css";

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

    /** Default duration of flash messages. */
    public static final Duration DEFAULT_FLASH_MESSAGE_DURATION = Duration.seconds(1.5);

    private Globals_GameUI() {}
}
