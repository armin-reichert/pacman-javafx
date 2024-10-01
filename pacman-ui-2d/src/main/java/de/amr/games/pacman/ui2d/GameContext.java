/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.maps.editor.TileMapEditor;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.page.EditorPage;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.page.Page;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import javafx.beans.property.ObjectProperty;

import java.util.Optional;

/**
 * @author Armin Reichert
 */
public interface GameContext {

    // Game model and controller
    ObjectProperty<GameVariant> gameVariantProperty();
    void selectGameVariant(GameVariant variant);
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

    // UI
    void selectPage(Page page);
    void selectStartPage();
    void selectGamePage();
    EditorPage getOrCreateEditorPage();
    GamePage gamePage();
    void showFlashMessage(String message, Object... args);
    void showFlashMessageSeconds(double seconds, String message, Object... args);

    /**
     * @return size (in tiles) of current world or size of Arcade world if no world currently exists
     */
    default Vector2i worldSizeTilesOrDefault() {
        boolean worldExists = game().world() != null;
        return new Vector2i(
            worldExists ? game().world().map().terrain().numCols() : GameModel.ARCADE_MAP_TILES_X,
            worldExists ? game().world().map().terrain().numRows() : GameModel.ARCADE_MAP_TILES_Y
        );
    }

    // Game scenes
    boolean currentGameSceneIs(GameSceneID gameSceneID);
    ObjectProperty<GameScene> gameSceneProperty();
    Optional<GameScene> currentGameScene();

    // Resources
    AssetStorage assets();

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

    default GameSpriteSheet spriteSheet() { return spriteSheet(game().variant()); }

    GameSpriteSheet spriteSheet(GameVariant variant);

    GameWorldRenderer renderer();

    default void attachRendererToCurrentMap(GameWorldRenderer renderer) {
        if (game().world() != null) {
            renderer.selectMap(game().world().map(), game().currentMapNumber(), spriteSheet());
        }
    }

    GameSounds sounds();

    // Actions
    void quitMapEditor(TileMapEditor editor);
    void restartIntro();
    void startCutscenesTest();
    void startGame();
    void startLevelTestMode();
    void toggle2D3D();
    void toggleDrawMode();
    void toggleImmunity();
    void togglePipVisible();
    void updateCustomMaps();

    // Others
    GameClockFX gameClock();
}