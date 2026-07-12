package de.amr.pacmanfx.ui.action.core;

public interface GameLifecycle {
    void startGamePlay();
    void suspendGamePlay();
    void terminate();
}
