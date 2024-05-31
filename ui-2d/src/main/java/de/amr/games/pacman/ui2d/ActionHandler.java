package de.amr.games.pacman.ui2d;

/**
 * @author Armin Reichert
 */
public interface ActionHandler {

    void showFlashMessage(String message, Object... args);

    void showFlashMessageSeconds(double seconds, String message, Object... args);

    void setFullScreen(boolean on);

    void selectPage(String pageID);

    void restartIntro();

    void reboot();

    void togglePaused();

    void doSimulationSteps(int numSteps);

    void changeSimulationSpeed(int delta);

    void resetSimulationSpeed();

    void toggleAutopilot();

    void startLevelTestMode();

    void toggleImmunity();

    void addCredit();

    void startGame();

    void selectNextGameVariant();

    void selectPrevGameVariant();

    void startCutscenesTest();

    void cheatEatAllPellets();

    void cheatAddLives();

    void cheatEnterNextLevel();

    void cheatKillAllEatableGhosts();
}