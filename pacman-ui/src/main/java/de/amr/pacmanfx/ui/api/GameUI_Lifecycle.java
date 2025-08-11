package de.amr.pacmanfx.ui.api;

public interface GameUI_Lifecycle {
    /**
     * Leaves the current game scene (if any) and displays the start page for the current game.
     */
    void quitCurrentGameScene();

    /**
     * Resets the game clock to normal speed and shows the boot screen for the selected game.
     */
    void restart();

    /**
     * Shows the start page for the given game variant, loads its resources and initializes the game model.
     *
     * @param gameVariant game variant name ("PACMAN", "MS_PACMAN" etc.)
     */
    void selectGameVariant(String gameVariant);

    /**
     * Shows the UI and displays the start page view.
     */
    void showUI();

    /**
     * Terminates the game and stops the game clock. Called when the application is terminated by closing the stage.
     */
    void terminate();
}