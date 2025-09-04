package de.amr.pacmanfx.tilemap.editor.rendering;

import de.amr.pacmanfx.lib.RectShort;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.tilemap.editor.EditorUtil.urlString;

public class ArcadeSprites {

    public static final Image SPRITE_SHEET = new Image(urlString("graphics/pacman_spritesheet.png"));

    public static final RectShort PAC_MAN      = new RectShort(473,  16, 14, 14);
    public static final RectShort RED_GHOST    = new RectShort(505,  65, 14, 14);
    public static final RectShort PINK_GHOST   = new RectShort(553,  81, 14, 14);
    public static final RectShort CYAN_GHOST   = new RectShort(521,  97, 14, 14);
    public static final RectShort ORANGE_GHOST = new RectShort(521, 113, 14, 14);
    public static final RectShort STRAWBERRY   = new RectShort(505,  49, 14, 14);

    // These are the colors from the first Ms. Pac-Man level
    public static final Color MS_PACMAN_COLOR_FOOD        = Color.web("#dedeff");
    public static final Color MS_PACMAN_COLOR_WALL_STROKE = Color.web("#ff0000");
    public static final Color MS_PACMAN_COLOR_WALL_FILL   = Color.web("#ffb7ae");
    public static final Color MS_PACMAN_COLOR_DOOR        = Color.web("#fcb5ff");
}