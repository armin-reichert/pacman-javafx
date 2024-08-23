/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.mapeditor.TileMapEditor;

/**
 * @author Armin Reichert
 */
public interface ActionHandler {
    void addCredit();
    void changeSimulationSpeed(int delta);
    void cheatAddLives();
    void cheatEatAllPellets();
    void cheatEnterNextLevel();
    void cheatKillAllEatableGhosts();
    void doSimulationSteps(int numSteps);
    void openMapEditor();
    void quitMapEditor(TileMapEditor editor);
    void reboot();
    void resetSimulationSpeed();
    void restartIntro();
    void selectEditorPage();
    void selectGamePage();
    void selectNextGameVariant();
    void selectNextPerspective();
    void selectPrevGameVariant();
    void selectPrevPerspective();
    void selectStartPage();
    void showFlashMessage(String message, Object... args);
    void showFlashMessageSeconds(double seconds, String message, Object... args);
    void startCutscenesTest();
    void startGame();
    void startLevelTestMode();
    void toggle2D3D();
    void toggleAutopilot();
    void toggleDashboard();
    void toggleDrawMode();
    void toggleImmunity();
    void togglePaused();
    void togglePipVisible();
    void updateCustomMaps();
}