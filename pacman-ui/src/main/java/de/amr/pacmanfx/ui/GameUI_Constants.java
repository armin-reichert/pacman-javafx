package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.ui.views.dashboard.DashboardConfig;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.rendering.Gradients;
import javafx.scene.layout.Background;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Arrays;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.uilib.UfxBackgrounds.createImageBackground;

public final class GameUI_Constants {

    public static final int MIN_STAGE_WIDTH  = 280;

    public static final int MIN_STAGE_HEIGHT = 360;

    public static final String RESOURCE_ROOT = "/de/amr/pacmanfx/ui/";

    /**
     * Resource manager for UI assets (fonts, images, sounds).
     */
    public static final ResourceManager RES_MGR = () -> GameUI_Constants.class;

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

    private GameUI_Constants() {}
}
