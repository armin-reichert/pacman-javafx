package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.ui2d.page.Page;

/**
 * @author Armin Reichert
 */
public interface ActionHandler {

    void showFlashMessage(String message, Object... args);

    void showFlashMessageSeconds(double seconds, String message, Object... args);

    void setFullScreen(boolean on);

    void selectStartPage();

    void selectGamePage();

    void selectEditorPage();

    void showSignature();

    void hideSignature();

    void restartIntro();

    void reboot();

    void enterMapEditor();

    void quitMapEditor();

    void togglePaused();

    void toggle2D3D();

    void togglePipVisible();

    void toggleDrawMode();

    void doSimulationSteps(int numSteps);

    void changeSimulationSpeed(int delta);

    void resetSimulationSpeed();

    void toggleAutopilot();

    void startLevelTestMode();

    void toggleImmunity();

    void toggleDashboard();

    void addCredit();

    void startGame();

    void selectNextGameVariant();

    void selectPrevGameVariant();

    void selectNextPerspective();

    void selectPrevPerspective();

    void startCutscenesTest();

    void cheatEatAllPellets();

    void cheatAddLives();

    void cheatEnterNextLevel();

    void cheatKillAllEatableGhosts();

}