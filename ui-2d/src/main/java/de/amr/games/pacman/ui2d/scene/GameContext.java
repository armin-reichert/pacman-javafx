/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.ActionHandler;
import de.amr.games.pacman.ui2d.GameSceneManager;
import de.amr.games.pacman.ui2d.SoundHandler;
import de.amr.games.pacman.ui2d.page.Page;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import de.amr.games.pacman.ui2d.util.Theme;
import javafx.beans.property.ObjectProperty;

import java.util.Optional;

/**
 * @author Armin Reichert
 */
public interface GameContext {

    GameClockFX gameClock();

    ActionHandler actionHandler();

    SoundHandler soundHandler();

    ObjectProperty<GameScene> gameSceneProperty();

    Optional<GameScene> currentGameScene();

    GameSceneManager gameSceneManager();

    default boolean isCurrentGameSceneRegisteredAs(GameSceneID sceneID) {
        return currentGameScene().isPresent() && isRegisteredAs(currentGameScene().get(), sceneID);
    }

    default boolean isRegisteredAs(GameScene gameScene, GameSceneID sceneID) {
        return gameSceneManager().isGameSceneRegisteredAs(gameScene, game().variant(), sceneID);
    }

    Theme theme();

    GameSpriteSheet getSpriteSheet(GameVariant variant);

    /**
     * Builds a resource key from the given key pattern and the arguments and returns the corresponding text from the
     * first resource bundle containing the key.
     *
     * @param key     key in resource bundle
     * @param args    optional arguments merged into the message (if pattern)
     * @return localized text with arguments merged or {@code "<key">} if no text is available
     */
    String tt(String key, Object... args);

    boolean isPageSelected(Page page);

    default GameController gameController() {
        return GameController.it();
    }

    default GameState gameState() {
        return GameController.it().state();
    }

    default GameModel game() {
        return GameController.it().game();
    }
}