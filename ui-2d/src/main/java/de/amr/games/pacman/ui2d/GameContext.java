/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.GameScene;
import de.amr.games.pacman.ui2d.scene.GameSceneID;
import de.amr.games.pacman.ui2d.util.AssetMap;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import javafx.beans.property.ObjectProperty;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

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
        return GameController.it().game();
    }

    void setScoreVisible(boolean visible);

    boolean isScoreVisible();

    // Game scenes

    boolean isCurrentGameSceneRegisteredAs(GameSceneID gameSceneID);

    ObjectProperty<GameScene> gameSceneProperty();

    Optional<GameScene> currentGameScene();

    // Resources

    AssetMap assets();

    GameSpriteSheet spriteSheet(GameVariant variant);

    List<ResourceBundle> bundles();

    /**
     * Builds a resource key from the given key pattern and the arguments and returns the corresponding text from the
     * first resource bundle containing the key.
     *
     * @param key     key in resource bundle
     * @param args    optional arguments merged into the message (if pattern)
     * @return localized text with arguments merged or {@code "<key">} if no text is available
     */
    String tt(String key, Object... args);

    // Others

    GameClockFX gameClock();

    ActionHandler actionHandler();

    SoundHandler soundHandler();
}