package de.amr.games.pacman.lib.arcade;

public interface Arcade {

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
