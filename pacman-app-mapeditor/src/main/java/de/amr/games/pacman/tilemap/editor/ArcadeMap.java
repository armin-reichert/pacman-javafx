package de.amr.games.pacman.tilemap.editor;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2i;
import javafx.scene.image.Image;

import static de.amr.games.pacman.tilemap.editor.TileMapEditorUtil.urlString;

public class ArcadeMap {

    public static final Image SPRITE_SHEET = new Image(urlString("graphics/pacman_spritesheet.png"));

    public static final RectArea PAC_SPRITE          = new RectArea(473,  16, 14, 14);
    public static final RectArea RED_GHOST_SPRITE    = new RectArea(505,  65, 14, 14);
    public static final RectArea PINK_GHOST_SPRITE   = new RectArea(553,  81, 14, 14);
    public static final RectArea CYAN_GHOST_SPRITE   = new RectArea(521,  97, 14, 14);
    public static final RectArea ORANGE_GHOST_SPRITE = new RectArea(521, 113, 14, 14);
    public static final RectArea BONUS_SPRITE        = new RectArea(505,  49, 14, 14);

    public static final String COLOR_FOOD = "rgb(255,255,255)";
    public static final String COLOR_WALL_STROKE = "rgb(33,33,255)";
    public static final String COLOR_WALL_FILL = "rgb(0,0,0)";
    public static final String COLOR_DOOR = "rgb(255,183, 255)";
    public static final Vector2i TILE_HOUSE = new Vector2i(10, 15);
    public static final Vector2i TILE_RED_GHOST = TILE_HOUSE.plus(3, -1);
    public static final Vector2i TILE_CYAN_GHOST = TILE_HOUSE.plus(1, 2);
    public static final Vector2i TILE_PINK_GHOST = TILE_HOUSE.plus(3, 2);
    public static final Vector2i TILE_ORANGE_GHOST = TILE_HOUSE.plus(5, 2);
    public static final Vector2i TILE_BONUS = new Vector2i(13, 20);
    public static final Vector2i TILE_PAC = new Vector2i(13, 26);
}
