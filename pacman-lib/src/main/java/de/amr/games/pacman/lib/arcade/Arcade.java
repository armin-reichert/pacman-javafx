package de.amr.games.pacman.lib.arcade;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;

public interface Arcade {

    public static final Vector2i ARCADE_MAP_SIZE_IN_TILES  = new Vector2i(28, 36);
    public static final Vector2f ARCADE_MAP_SIZE_IN_PIXELS = new Vector2f(224, 288);

    interface Palette {

        String RED    = "rgb(255, 0, 0)";
        String YELLOW = "rgb(255, 255, 0)";
        String PINK   = "rgb(252, 181, 255)";
        String CYAN   = "rgb(0, 255, 255)";
        String ORANGE = "rgb(251, 190, 88)";
        String BLUE   = "rgb(33, 33, 255)";
        String WHITE  = "rgb(222, 222, 255)";
        String ROSE   = "rgb(252, 187, 179)";
    }

    enum Button {
        START, COIN, UP, DOWN, LEFT, RIGHT;
    }
}
