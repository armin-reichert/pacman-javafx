/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.maps.editor.TileMapEditor;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.util.AssetMap;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import javafx.beans.property.ObjectProperty;

import java.util.Optional;

/**
 * @author Armin Reichert
 */
public interface GameContext {

    // Game model and controller
    default GameController gameController() {
        return GameController.it();
    }
    default GameState gameState() {
        return GameController.it().state();
    }
    default GameModel game() {
        return GameController.it().currentGame();
    }
    void setScoreVisible(boolean visible);
    boolean isScoreVisible();

    // Game scenes
    boolean currentGameSceneIs(GameSceneID gameSceneID);
    ObjectProperty<GameScene> gameSceneProperty();
    Optional<GameScene> currentGameScene();

    // Resources
    AssetMap assets();
    GameSpriteSheet spriteSheet(GameVariant variant);
    /**
     * Returns a translated text (for the current locale).
     * <p></p>
     * The key is constructed using the given key pattern and the arguments.
     *
     * @param keyOrPattern     key in resource bundle
     * @param args    optional arguments merged into the key pattern
     * @return localized text with constructed key or default text if no such key exists
     */
    String locText(String keyOrPattern, Object... args);

    // Actions
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

    // Others
    GameClockFX gameClock();
}