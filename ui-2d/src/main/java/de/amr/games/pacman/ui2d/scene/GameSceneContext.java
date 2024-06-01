package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.ActionHandler;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.util.GameClockFX;
import de.amr.games.pacman.ui2d.util.Theme;
import javafx.beans.property.ObjectProperty;
import javafx.scene.media.AudioClip;

import java.util.Map;
import java.util.Optional;

/**
 * @author Armin Reichert
 */
public interface GameSceneContext {

    GameClockFX gameClock();

    ActionHandler actionHandler();

    ObjectProperty<GameScene> gameSceneProperty();

    Optional<GameScene> currentGameScene();

    Map<String, GameScene> gameScenesForCurrentGameVariant();

    default boolean isCurrentGameScene(String sceneID) {
        return currentGameScene().isPresent() && gameScenesForCurrentGameVariant().get(sceneID) == currentGameScene().get();
    }

    default boolean isGameScene(GameScene gameScene, String sceneID) {
        return gameScenesForCurrentGameVariant().get(sceneID) == gameScene;
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

    default GameController gameController() {
        return GameController.it();
    }

    default GameState gameState() {
        return GameController.it().state();
    }

    default GameModel game() {
        return GameController.it().game();
    }

    AudioClip audioClip(String key);

    default void playAudioClip(String key) {
        audioClip(key).play();
    }

    default void stopAudioClip(String key) {
        audioClip(key).stop();
    }

    void stopAllSounds();

    void ensureSirenStarted(int sirenIndex);

    void stopSirens();

    void ensureAudioLoop(String key, int repetitions);

    void ensureAudioLoop(String key);
}