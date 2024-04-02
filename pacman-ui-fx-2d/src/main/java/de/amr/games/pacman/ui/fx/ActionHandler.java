package de.amr.games.pacman.ui.fx;

/**
 * @author Armin Reichert
 */
public interface ActionHandler {

    void showFlashMessage(String message, Object... args);

    void showFlashMessageSeconds(double seconds, String message, Object... args);

    void setFullScreen(boolean on);

    void restartIntro();

    void reboot();

    void togglePaused();

    void doSimulationSteps(int numSteps);

    void changeSimulationSpeed(int delta);

    void resetSimulationSpeed();

    void toggleAutopilot();

    void enterLevel(int newLevelNumber);

    void startLevelTestMode();

    void toggleImmunity();

    void addCredit();

    void startGame();

    void switchGameVariant();

    void startCutscenesTest();

    void cheatEatAllPellets();

    void cheatAddLives();

    void cheatEnterNextLevel();

    void cheatKillAllEatableGhosts();
    void toggle2D3D();
}