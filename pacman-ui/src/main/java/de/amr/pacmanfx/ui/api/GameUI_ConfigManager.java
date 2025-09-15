package de.amr.pacmanfx.ui.api;

public interface GameUI_ConfigManager {
    /**
     * @param gameVariant name of game variant
     * @return UI configuration for given game variant
     */
    GameUI_Config config(String gameVariant);

    /**
     * @return UI configuration for the current game
     * @param <T> type of UI configuration
     */
    <T extends GameUI_Config> T currentConfig();
}
