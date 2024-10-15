/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.page.EditorPage;
import de.amr.games.pacman.ui2d.page.GamePage;
import de.amr.games.pacman.ui2d.page.Page;
import de.amr.games.pacman.ui2d.scene.common.GameScene;
import de.amr.games.pacman.ui2d.scene.common.GameSceneConfiguration;
import de.amr.games.pacman.ui2d.sound.GameSounds;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import de.amr.games.pacman.ui2d.util.Keyboard;
import javafx.beans.property.ObjectProperty;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public interface GameContext {

    // Game model and controller
    default GameController      gameController() { return GameController.it(); }
    default GameState           gameState() { return GameController.it().state(); }
    ObjectProperty<GameVariant> gameVariantProperty();
    default GameVariant         gameVariant() { return GameController.it().currentGame().variant(); }
    void                        selectGameVariant(GameVariant variant);
    default GameModel           game() { return GameController.it().currentGame(); }
    void                        setScoreVisible(boolean visible);
    boolean                     isScoreVisible();
    Vector2i                    worldSizeTilesOrDefault();

    // UI
    Keyboard    keyboard();
    void        selectPage(Page page);
    void        selectStartPage();
    void        selectGamePage();
    EditorPage  getOrCreateEditorPage();
    GamePage    gamePage();
    void        showFlashMessage(String message, Object... args);
    void        showFlashMessageSeconds(double seconds, String message, Object... args);
    GameClockFX gameClock();
    Vector2f    sceneSize();

    // Actions
    boolean      isActionCalled(GameAction action);
    void         doFirstCalledAction(Stream<GameAction> actions);
    default void doFirstCalledAction(Collection<GameAction> actions) { doFirstCalledAction(actions.stream()); }
    default void doFirstCalledAction(GameAction... actions) { doFirstCalledAction(Stream.of(actions)); }
    void         doFirstCalledActionOrElse(Stream<GameAction> actions, Runnable defaultAction);
    default void doFirstCalledActionOrElse(Collection<GameAction> actions, Runnable defaultAction) { doFirstCalledActionOrElse(actions.stream(), defaultAction); }

    // Game scenes
    GameSceneConfiguration         gameSceneConfig(GameVariant variant);
    default GameSceneConfiguration currentGameSceneConfig() { return gameSceneConfig(gameVariant()); }
    boolean                        currentGameSceneHasID(String gameSceneID);
    ObjectProperty<GameScene>      gameSceneProperty();
    Optional<GameScene>            currentGameScene();
    void                           updateGameScene(boolean reloadCurrent);

    // Resources
    AssetStorage assets();
    GameSounds   sounds();

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
}