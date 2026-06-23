/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.rendering.Gradients;
import javafx.scene.layout.Background;
import javafx.scene.media.Media;
import javafx.scene.text.Font;
import org.tinylog.Logger;

public final class GlobalAssets {

    private GlobalAssets() {}

    public static final String RESOURCE_ROOT = "/de/amr/pacmanfx/ui/";

    public static final String GAME_STYLESHEET = RESOURCE_ROOT + "css/game.css";

    public static final ResourceManager RES_MGR = () -> GlobalAssets.class;

    public enum PredefinedFont {
        ARCADE6       ("fonts/emulogic.ttf", 6),
        ARCADE8       ("fonts/emulogic.ttf", 8),
        HANDWRITING   ("fonts/Molle-Italic.ttf", 9),
        MONOSPACED    ("fonts/fantasquesansmono-bold.otf", 12),
        PAC_FONT_GOOD ("fonts/PacfontGood.ttf", 8);

        PredefinedFont(String path, double size) {
            font = RES_MGR.loadFont(RESOURCE_ROOT + path, size);
        }

        public Font font() {
            return font;
        }

        public Font font(double size) {
            return Font.font(font.getFamily(), size);
        }

        private final Font font;
    }

    public enum Voice {
        AUTOPILOT_ON       ("sound/voice/autopilot-on.mp3"),
        AUTOPILOT_OFF      ("sound/voice/autopilot-off.mp3"),
        IMMUNITY_ON        ("sound/voice/immunity-on.mp3"),
        IMMUNITY_OFF       ("sound/voice/immunity-off.mp3"),
        EXPLAIN_GAME_START ("sound/voice/press-key.mp3");

        Voice(String path) {
            media = RES_MGR.loadMedia(path);
        }

        public Media media() {
            return media;
        }

        private final Media media;
    }

    public static final Background BACKGROUND_PAC_MAN_WALLPAPER = Ufx.createImageBackground(
        RES_MGR.loadImage(RESOURCE_ROOT + "graphics/pacman_wallpaper.png"));

    public static final Background[] GRADIENT_BACKGROUNDS = Gradients.Samples.backgrounds();

    static {
        Logger.info("Loading predefined fonts");
        for (var f : PredefinedFont.values()) {
            Logger.info(f.font());
        }
    }
}
