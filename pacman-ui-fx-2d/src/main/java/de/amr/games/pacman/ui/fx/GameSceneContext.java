package de.amr.games.pacman.ui.fx;

import de.amr.games.pacman.controller.GameController;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui.fx.util.GameClock;
import de.amr.games.pacman.ui.fx.util.SpriteSheet;
import de.amr.games.pacman.ui.fx.util.Theme;
import javafx.beans.property.ObjectProperty;
import javafx.scene.media.AudioClip;
import org.tinylog.Logger;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.games.pacman.lib.Globals.checkNotNull;

/**
 * @author Armin Reichert
 */
public interface GameSceneContext {

    static String message(List<ResourceBundle> bundles, String key, Object... args) {
        checkNotNull(key);
        for (var bundle : bundles) {
            if (bundle.containsKey(key)) {
                return MessageFormat.format(bundle.getString(key), args);
            }
        }
        Logger.error("Missing localized text for key {}", key);
        return null;
    }

    GameClock gameClock();

    ActionHandler actionHandler();

    ObjectProperty<GameScene> gameSceneProperty();

    Optional<GameScene> currentGameScene();

    Map<String, GameScene> sceneConfig();

    Theme theme();

    /**
     * Builds a resource key from the given key pattern and the arguments and returns the corresponding text from the
     * first resource bundle containing the key.
     *
     * @param key     key in resource bundle
     * @param args    optional arguments merged into the message (if pattern)
     * @return localized text with arguments merged or {@code "<key">} if no text is available
     */
    String tt(String key, Object... args);

    <S extends SpriteSheet> S spriteSheet();

    default GameController gameController() {
        return GameController.it();
    }

    default GameState gameState() {
        return GameController.it().state();
    }

    default GameModel game() {
        return GameController.it().game();
    }

    default GameVariant gameVariant() {
        return game().variant();
    }

    default Optional<GameLevel> gameLevel() {
        return game().level();
    }

    AudioClip audioClip(String key);

    default void playAudioClip(String key) {
        audioClip(key).play();
    }

    default void stopAudioClip(String key) {
        audioClip(key).stop();
    }

    void ensureSirenStarted(int sirenIndex);

    void stopSirens();

    void ensureAudioLoop(String key, int repetitions);

    void ensureAudioLoop(String key);
}