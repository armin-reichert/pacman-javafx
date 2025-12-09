/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.uilib.assets.LocalizedTextAccessor;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.layout.Background;
import javafx.scene.media.Media;
import javafx.scene.text.Font;

import java.util.ResourceBundle;

import static de.amr.pacmanfx.uilib.Ufx.createImageBackground;

/**
 * Global assets used in the Pac-Man games UI.
 */
public class GlobalGameAssets implements LocalizedTextAccessor {

    public static final String STYLE_SHEET_PATH = "/de/amr/pacmanfx/ui/css/style.css";

    private static final ResourceManager GLOBAL_RESOURCES = () -> GameUI_Implementation.class;

    private final ResourceBundle localizedTexts;

    public final Background background_PacManWallpaper;

    public final Font font_Arcade_8;
    public final Font font_Arcade_6;
    public final Font font_Handwriting;
    public final Font font_Monospaced;
    public final Font font_PacFont;
    public final Font font_PacFontGood;

    public final Media voice_Autopilot_Off;
    public final Media voice_Autopilot_On;
    public final Media voice_Explain;
    public final Media voice_Immunity_Off;
    public final Media voice_Immunity_On;

    public GlobalGameAssets() {
        localizedTexts = GLOBAL_RESOURCES.getModuleBundle("de.amr.pacmanfx.ui.localized_texts");

        background_PacManWallpaper = createImageBackground(GLOBAL_RESOURCES.loadImage("graphics/pacman_wallpaper.png"));

        font_Arcade_6 = GLOBAL_RESOURCES.loadFont("fonts/emulogic.ttf", 6);
        font_Arcade_8 = GLOBAL_RESOURCES.loadFont("fonts/emulogic.ttf", 8);
        font_Handwriting = GLOBAL_RESOURCES.loadFont("fonts/Molle-Italic.ttf", 9);
        font_Monospaced = GLOBAL_RESOURCES.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12);
        font_PacFont = GLOBAL_RESOURCES.loadFont("fonts/Pacfont.ttf", 8);
        font_PacFontGood = GLOBAL_RESOURCES.loadFont("fonts/PacfontGood.ttf", 8);

        voice_Autopilot_Off = new Media(GLOBAL_RESOURCES.url("sound/voice/autopilot-off.mp3").toExternalForm());
        voice_Autopilot_On  = new Media(GLOBAL_RESOURCES.url("sound/voice/autopilot-on.mp3").toExternalForm());
        voice_Explain       = new Media(GLOBAL_RESOURCES.url("sound/voice/press-key.mp3").toExternalForm());
        voice_Immunity_Off  = new Media(GLOBAL_RESOURCES.url("sound/voice/immunity-off.mp3").toExternalForm());
        voice_Immunity_On   = new Media(GLOBAL_RESOURCES.url("sound/voice/immunity-on.mp3").toExternalForm());
    }

    @Override
    public ResourceBundle localizedTexts() {
        return localizedTexts;
    }
}