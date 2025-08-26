package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.model.DefaultHUDData;

public class TengenMsPacMan_HUDData extends DefaultHUDData {

    private boolean gameOptionsVisible = false;

    public void showGameOptions(boolean visible) {
        gameOptionsVisible = visible;
    }

    public boolean gameOptionsVisible() {
        return gameOptionsVisible;
    }
}
