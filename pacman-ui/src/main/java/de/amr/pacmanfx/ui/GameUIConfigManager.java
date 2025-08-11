package de.amr.pacmanfx.ui;

public interface GameUIConfigManager {
    /**
     * @param gameVariant name of game variant
     * @return UI configuration for given game variant
     */
    GameUI_Config config(String gameVariant);

    /**
     * Stores the UI configuration for a game variant and initializes the game scenes (assigns the game context).
     *
     * @param variant a game variant
     * @param config the UI configuration for this variant
     */
    void setConfig(String variant, GameUI_Config config);

    /**
     * @return UI configuration for the current game
     * @param <T> type of UI configuration
     */
    <T extends GameUI_Config> T currentConfig();
}
