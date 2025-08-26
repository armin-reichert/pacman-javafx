package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.model.DefaultHUDControlData;

public class TengenMsPacMan_HUDControlData extends DefaultHUDControlData {

    private boolean levelNumberVisible;
    private boolean gameOptionsVisible;

    public void showGameOptions(boolean visible) {
        gameOptionsVisible = visible;
    }

    public boolean gameOptionsVisible() {
        return gameOptionsVisible;
    }

    public void showLevelNumber(boolean visible) {
        levelNumberVisible = visible;
    }

    public boolean levelNumberVisible() {
        return levelNumberVisible;
    }
}
