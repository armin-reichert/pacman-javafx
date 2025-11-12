/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.uilib.assets.LocalizedTextAccessor;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.layout.Background;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.uilib.Ufx.createImageBackground;
import static java.util.Objects.requireNonNull;

/**
 * Global assets used in the Pac-Man games UI.
 */
public class GlobalGameAssets implements LocalizedTextAccessor {

    public static final String STYLE_SHEET_PATH = "/de/amr/pacmanfx/ui/css/style.css";

    private static final ResourceManager GLOBAL_RESOURCES = () -> GameUI_Implementation.class;

    private final ResourceBundle localizedTexts;

    public final Background background_PacManWallpaper;

    public final Font font_Arcade;
    public final Font font_Handwriting;
    public final Font font_Monospaced;
    public final Font font_PacFont;
    public final Font font_PacFontGood;

    public final URL voice_Autopilot_Off;
    public final URL voice_Autopilot_On;
    public final URL voice_Explain;
    public final URL voice_Immunity_Off;
    public final URL voice_Immunity_On;

    public GlobalGameAssets() {
        localizedTexts = GLOBAL_RESOURCES.getModuleBundle("de.amr.pacmanfx.ui.localized_texts");

        background_PacManWallpaper = createImageBackground(GLOBAL_RESOURCES.loadImage("graphics/pacman_wallpaper.png"));

        font_Arcade = GLOBAL_RESOURCES.loadFont("fonts/emulogic.ttf", 8);
        font_Handwriting = GLOBAL_RESOURCES.loadFont("fonts/Molle-Italic.ttf", 9);
        font_Monospaced = GLOBAL_RESOURCES.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12);
        font_PacFont = GLOBAL_RESOURCES.loadFont("fonts/Pacfont.ttf", 8);
        font_PacFontGood = GLOBAL_RESOURCES.loadFont("fonts/PacfontGood.ttf", 8);

        voice_Explain = GLOBAL_RESOURCES.url("sound/voice/press-key.mp3");
        voice_Autopilot_Off = GLOBAL_RESOURCES.url("sound/voice/autopilot-off.mp3");
        voice_Autopilot_On = GLOBAL_RESOURCES.url("sound/voice/autopilot-on.mp3");
        voice_Immunity_Off = GLOBAL_RESOURCES.url("sound/voice/immunity-off.mp3");
        voice_Immunity_On = GLOBAL_RESOURCES.url("sound/voice/immunity-on.mp3");
    }

    @Override
    public ResourceBundle localizedTexts() {
        return localizedTexts;
    }

    public Font arcadeFont(double size) { return Font.font(font_Arcade.getFamily(), size); }
}