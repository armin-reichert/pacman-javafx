package de.amr.pacmanfx.tengen.ms_pacman.model;

import de.amr.pacmanfx.model.DefaultHUDControlData;

public class TengenMsPacMan_HUDControlData extends DefaultHUDControlData {

    private boolean gameOptionsVisible = false;

    public void showGameOptions(boolean visible) {
        gameOptionsVisible = visible;
    }

    public boolean gameOptionsVisible() {
        return gameOptionsVisible;
    }
}
