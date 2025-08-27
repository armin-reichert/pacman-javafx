package de.amr.pacmanfx.tilemap.editor.rendering;

import de.amr.pacmanfx.lib.RectShort;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.urlString;

public class ArcadeSprites {

    public static final Image SPRITE_SHEET = new Image(urlString("graphics/pacman_spritesheet.png"));

    public static final RectShort PAC_MAN = new RectShort(473,  16, 14, 14);
    public static final RectShort RED_GHOST = new RectShort(505,  65, 14, 14);
    public static final RectShort PINK_GHOST = new RectShort(553,  81, 14, 14);
    public static final RectShort CYAN_GHOST = new RectShort(521,  97, 14, 14);
    public static final RectShort ORANGE_GHOST = new RectShort(521, 113, 14, 14);
    public static final RectShort STRAWBERRY = new RectShort(505,  49, 14, 14);

    // These are the colors from the first Ms. Pac-Man level
    public static final String MS_PACMAN_COLOR_FOOD = "#dedeff";
    public static final String MS_PACMAN_COLOR_WALL_STROKE = "#ff0000";
    public static final String MS_PACMAN_COLOR_WALL_FILL = "#ffb7ae";
    public static final String MS_PACMAN_COLOR_DOOR = "#fcb5ff";
}