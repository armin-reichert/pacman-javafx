/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.model.world.map.GenericWorldMapColorScheme;
import de.amr.pacmanfx.ui.settings.world.WorldSettings;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.rendering.EggradientSamples;
import javafx.scene.layout.Background;
import javafx.scene.media.Media;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

public final class GlobalAssets {

    private GlobalAssets() {}

    public static final String RESOURCE_ROOT = "/de/amr/pacmanfx/ui/";

    public static final String GAME_STYLESHEET = RESOURCE_ROOT + "css/game.css";

    public static final ResourceManager RES_MGR = () -> GlobalAssets.class;

    public enum Fonts {
        ARCADE6       ("fonts/emulogic.ttf", 6),
        ARCADE8       ("fonts/emulogic.ttf", 8),
        HANDWRITING   ("fonts/Molle-Italic.ttf", 9),
        MONOSPACED    ("fonts/fantasquesansmono-bold.otf", 12),
        PAC_FONT_GOOD ("fonts/PacfontGood.ttf", 8);

        Fonts(String path, double size) {
            font = RES_MGR.loadFont(RESOURCE_ROOT + path, size);
        }

        public javafx.scene.text.Font font() {
            return font;
        }

        public javafx.scene.text.Font font(double size) {
            return javafx.scene.text.Font.font(font.getFamily(), size);
        }

        private final javafx.scene.text.Font font;
    }

    public enum VoiceID {
        AUTOPILOT_ON       ("sound/voice/autopilot-on.mp3"),
        AUTOPILOT_OFF      ("sound/voice/autopilot-off.mp3"),
        IMMUNITY_ON        ("sound/voice/immunity-on.mp3"),
        IMMUNITY_OFF       ("sound/voice/immunity-off.mp3"),
        EXPLAIN_GAME_START ("sound/voice/press-key.mp3");

        VoiceID(String path) {
            media = RES_MGR.loadMedia(path);
        }

        public Media media() {
            return media;
        }

        private final Media media;
    }

    public static final Background BACKGROUND_PAC_MAN_WALLPAPER = Ufx.createImageBackground(
        RES_MGR.loadImage(RESOURCE_ROOT + "graphics/pacman_wallpaper.png"));

    public static final Background[] GRADIENT_BACKGROUNDS = EggradientSamples.backgrounds();

    static {
        Logger.info("Loading predefined fonts");
        for (var predefinedFont : Fonts.values()) {
            Logger.info(predefinedFont.font());
        }
    }

    public static GenericWorldMapColorScheme enhanceContrast(WorldSettings worldSettings, GenericWorldMapColorScheme colorScheme) {
        final Color wallFillColor = Color.valueOf(colorScheme.wallFill());
        if (wallFillColor.getBrightness() < 0.1) {
            return new GenericWorldMapColorScheme(
                worldSettings.maze().darkWallFillColor(),
                colorScheme.wallStroke(),
                colorScheme.door(),
                colorScheme.pellet());
        }
        return colorScheme;
    }
}
