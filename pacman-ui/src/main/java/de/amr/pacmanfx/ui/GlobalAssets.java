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

    private static final ResourceManager RES_MGR = () -> GlobalAssets.class;

    public static final Background BACKGROUND_PAC_MAN_WALLPAPER = Ufx.createImageBackground(
        RES_MGR.loadImage(RESOURCE_ROOT + "graphics/pacman_wallpaper.png"));

    public static final Background[] GRADIENT_BACKGROUNDS = EggradientSamples.backgrounds();

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
