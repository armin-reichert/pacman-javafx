/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.layout.Background;
import javafx.scene.media.Media;
import javafx.scene.text.Font;

import java.util.ResourceBundle;

import static de.amr.pacmanfx.uilib.Ufx.createImageBackground;

/**
 * Global assets used in the Pac-Man games UI.
 */
public interface GameUI_Assets {

    String STYLE_SHEET_PATH = "/de/amr/pacmanfx/ui/css/style.css";

    ResourceManager GLOBAL_RESOURCES = () -> GameUI_Implementation.class;

    ResourceBundle LOCALIZED_TEXTS = GLOBAL_RESOURCES.getModuleBundle("de.amr.pacmanfx.ui.localized_texts");

    Background BACKGROUND_PAC_MAN_WALLPAPER = createImageBackground(GLOBAL_RESOURCES.loadImage("graphics/pacman_wallpaper.png"));

    Font FONT_ARCADE_6 = GLOBAL_RESOURCES.loadFont("fonts/emulogic.ttf", 6);
    Font FONT_ARCADE_8 = GLOBAL_RESOURCES.loadFont("fonts/emulogic.ttf", 8);
    Font FONT_HANDWRITING = GLOBAL_RESOURCES.loadFont("fonts/Molle-Italic.ttf", 9);
    Font FONT_MONOSPACED = GLOBAL_RESOURCES.loadFont("fonts/Inconsolata_Condensed-Bold.ttf", 12);
    Font FONT_PAC_FONT = GLOBAL_RESOURCES.loadFont("fonts/Pacfont.ttf", 8);
    Font FONT_PAC_FONT_GOOD = GLOBAL_RESOURCES.loadFont("fonts/PacfontGood.ttf", 8);

    Media VOICE_AUTOPILOT_OFF = new Media(GLOBAL_RESOURCES.url("sound/voice/autopilot-off.mp3").toExternalForm());
    Media VOICE_AUTOPILOT_ON = new Media(GLOBAL_RESOURCES.url("sound/voice/autopilot-on.mp3").toExternalForm());
    Media VOICE_EXPLAIN = new Media(GLOBAL_RESOURCES.url("sound/voice/press-key.mp3").toExternalForm());
    Media VOICE_IMMUNITY_OFF = new Media(GLOBAL_RESOURCES.url("sound/voice/immunity-off.mp3").toExternalForm());
    Media VOICE_IMMUNITY_ON = new Media(GLOBAL_RESOURCES.url("sound/voice/immunity-on.mp3").toExternalForm());
}