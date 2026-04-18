/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.mapeditor.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class ArcadeSprites {

    private static final ResourceManager RESOURCE_MANAGER = () -> ArcadeSprites.class;

    public static final Image SPRITE_SHEET = RESOURCE_MANAGER.loadImage("/de/amr/pacmanfx/mapeditor/graphics/pacman_spritesheet.png");

    public static final RectShort PAC_MAN      = RectShort.of(473,  16, 14, 14);
    public static final RectShort RED_GHOST    = RectShort.of(505,  65, 14, 14);
    public static final RectShort PINK_GHOST   = RectShort.of(553,  81, 14, 14);
    public static final RectShort CYAN_GHOST   = RectShort.of(521,  97, 14, 14);
    public static final RectShort ORANGE_GHOST = RectShort.of(521, 113, 14, 14);
    public static final RectShort STRAWBERRY   = RectShort.of(505,  49, 14, 14);

    // These are the colors from the first Ms. Pac-Man level
    public static final Color MS_PACMAN_COLOR_FOOD        = Color.valueOf("#dedeff");
    public static final Color MS_PACMAN_COLOR_WALL_STROKE = Color.valueOf("#ff0000");
    public static final Color MS_PACMAN_COLOR_WALL_FILL   = Color.valueOf("#ffb7ae");
    public static final Color MS_PACMAN_COLOR_DOOR        = Color.valueOf("#fcb5ff");
}