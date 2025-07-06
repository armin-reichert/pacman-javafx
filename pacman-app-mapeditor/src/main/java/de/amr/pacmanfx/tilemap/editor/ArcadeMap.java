package de.amr.pacmanfx.tilemap.editor;

import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2i;
import javafx.scene.image.Image;

import static de.amr.pacmanfx.tilemap.editor.TileMapEditorUtil.urlString;

public class ArcadeMap {

    public static final Image SPRITE_SHEET = new Image(urlString("graphics/pacman_spritesheet.png"));

    public static final RectShort PAC_SPRITE          = new RectShort(473,  16, 14, 14);
    public static final RectShort RED_GHOST_SPRITE    = new RectShort(505,  65, 14, 14);
    public static final RectShort PINK_GHOST_SPRITE   = new RectShort(553,  81, 14, 14);
    public static final RectShort CYAN_GHOST_SPRITE   = new RectShort(521,  97, 14, 14);
    public static final RectShort ORANGE_GHOST_SPRITE = new RectShort(521, 113, 14, 14);
    public static final RectShort BONUS_SPRITE        = new RectShort(505,  49, 14, 14);

    public static final String MS_PACMAN_COLOR_FOOD = "#dedeff";
    public static final String MS_PACMAN_COLOR_WALL_STROKE = "#ff0000";
    public static final String MS_PACMAN_COLOR_WALL_FILL = "#ffb7ae";
    public static final String MS_PACMAN_COLOR_DOOR = "#fcb5ff";

    public static final Vector2i TILE_HOUSE = new Vector2i(10, 15);
    public static final Vector2i TILE_RED_GHOST = TILE_HOUSE.plus(3, -1);
    public static final Vector2i TILE_CYAN_GHOST = TILE_HOUSE.plus(1, 2);
    public static final Vector2i TILE_PINK_GHOST = TILE_HOUSE.plus(3, 2);
    public static final Vector2i TILE_ORANGE_GHOST = TILE_HOUSE.plus(5, 2);
    public static final Vector2i TILE_BONUS = new Vector2i(13, 20);
    public static final Vector2i TILE_PAC = new Vector2i(13, 26);
}