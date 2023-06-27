package de.amr.games.pacman.ui.fx.app;

public interface ActionHandler {

    void showFlashMessage(String message, Object... args);

    void showFlashMessageSeconds(double seconds, String message, Object... args);

    void restartIntro();

    void reboot();

    void togglePaused();

    void oneSimulationStep();

    void tenSimulationSteps();

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
}
