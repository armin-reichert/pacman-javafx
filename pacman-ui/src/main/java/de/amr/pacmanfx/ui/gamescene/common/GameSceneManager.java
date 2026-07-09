/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.GameVariantConfig;
import de.amr.pacmanfx.ui.game.Game;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d3.GameLevel3D;
import de.amr.pacmanfx.ui.gamescene.d3.PlayScene3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.views.playview.DecorationPane;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameSceneManager {

    private Game game;

    private final ObjectProperty<GameScene> currentGameScene = new SimpleObjectProperty<>();

    public GameSceneManager() {
        currentGameScene.addListener((_, _, newGameScene) -> {
            if (newGameScene != null) {
                game.ui().viewManager().gamePlayView().embedGameScene(newGameScene);
            }
        });
    }

    public void connect(Game game) {
        this.game = requireNonNull(game);
    }

    public Optional<GameScene> optCurrentGameScene() {
        return Optional.ofNullable(currentGameScene.get());
    }

    public ObjectProperty<GameScene> currentGameSceneProperty() {
        return currentGameScene;
    }

    public void forceGameSceneUpdate() {
        updateGameSceneAndForceReload(true);
    }

    public void updateGameSceneAndForceReload(boolean forceReload) {
        final GameVariantConfig variantConfig = game.variantManager().selectedVariant().config();
        final GameModel model = game.context().model();

        final GameScene currentGameScene = optCurrentGameScene().orElse(null);
        final GameScene nextGameScene = variantConfig.gameSceneConfig().selectGameScene(game, model).orElseThrow();

        if (nextGameScene == currentGameScene && !forceReload) {
            return;
        }

        if (currentGameScene != null) {
            currentGameScene.deactivate();
            removeFromPlayView(currentGameScene);
        }

        nextGameScene.onEmbedded(); // Must be called *before* embedding
        game.ui().viewManager().gamePlayView().embedGameScene(nextGameScene);
        nextGameScene.activate();
        model.optLevel().ifPresent(level -> handle2D3DSwitch(variantConfig, level, currentGameScene, nextGameScene));
        currentGameSceneProperty().set(nextGameScene);
    }

    /**
     * Checks whether the given game scene matches the given ID.
     *
     * @param gameScene game scene
     * @param sceneID scene identifier
     * @return {@code true} if the active scene has the given ID
     */
    public boolean hasGameSceneID(GameScene gameScene, Identifier sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        final GameVariantConfig gameVariantConfig = game.variantManager().selectedVariant().config();
        return gameVariantConfig.gameSceneConfig().gameSceneHasID(gameScene, sceneID);
    }

    /**
     * Checks whether the current game scene matches the given ID.
     *
     * @param sceneID scene identifier
     * @return {@code true} if the active scene has the given ID
     */
    public boolean currentGameSceneHasID(Identifier sceneID) {
        final GameScene current = currentGameSceneProperty().get();
        return current != null && hasGameSceneID(current, sceneID);
    }

    // 2D-3D scene switch

    private void handle2D3DSwitch(GameVariantConfig gameVariant, GameLevel level, GameScene prevGameScene, GameScene nextGameScene) {
        final GameSceneSwitchType sceneSwitchType = identifySceneSwitchType(prevGameScene, nextGameScene);
        switch (sceneSwitchType) {
            case FROM_2D_TO_3D -> switchPlaySceneTo3D(gameVariant, level, prevGameScene, nextGameScene);
            case FROM_3D_TO_2D -> switchPlaySceneTo2D(prevGameScene, nextGameScene);
            case NONE -> {}
            default -> throw new IllegalArgumentException("Illegal scene switch type: " + sceneSwitchType);
        }
    }

    private void switchPlaySceneTo3D(GameVariantConfig gameVariant, GameLevel level, GameScene currentScene, GameScene nextScene) {
        if (!(nextScene instanceof PlayScene3D playScene3D)) {
            throw new IllegalArgumentException("Expected PlayScene3D, but scene has class %s"
                .formatted(nextScene.getClass().getSimpleName()));
        }

        playScene3D.replaceGameLevel3D(level);
        playScene3D.updateHUD3D(level);
        playScene3D.replaceActionBindings(level);
        playScene3D.initFood3D(level, true);

        final GameLevel3D level3D = playScene3D.optGameLevel3D().orElseThrow();
        final Pac3D pac3D = level3D.entities().pac3D();
        playScene3D.initPac3D(pac3D, level);
        level3D.startLivesCounterTrackingPac();

        if (level.entities().pac().powerTimer().isRunning()) {
            gameVariant.optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
        }

        Logger.info("3D scene {} entered from 2D game scene {}", playScene3D.getClass().getSimpleName(), currentScene.getClass().getSimpleName());

        playScene3D.fadeInAnimation().playFromStart();
    }

    private void switchPlaySceneTo2D(GameScene currentScene, GameScene nextScene) {
        if (!(nextScene instanceof AbstractGameScene2D playScene2D)) {
            throw new IllegalArgumentException("Expected GameScene2D, but scene has class %s"
                .formatted(nextScene.getClass().getSimpleName()));
        }
        playScene2D.onEnteredFrom3DScene();
        Logger.info("2D scene {} entered from 3D scene {}", playScene2D.getClass().getSimpleName(), currentScene.getClass().getSimpleName());
    }

    private GameSceneSwitchType identifySceneSwitchType(GameScene sceneBefore, GameScene sceneAfter) {
        if (sceneBefore == null && sceneAfter == null) {
            throw new IllegalStateException("WTF is going on here, switch between NULL scenes?");
        }
        return switch (sceneBefore) {
            case AbstractGameScene2D ignored when sceneAfter instanceof PlayScene3D -> GameSceneSwitchType.FROM_2D_TO_3D;
            case PlayScene3D ignored when sceneAfter instanceof AbstractGameScene2D -> GameSceneSwitchType.FROM_3D_TO_2D;
            case null, default -> GameSceneSwitchType.NONE; // may happen, it's ok
        };
    }

    // Scene embedding

    public void removeFromPlayView(GameScene gameScene) {
        requireNonNull(game);
        requireNonNull(gameScene);

        game.ui().viewManager().gamePlayView().contextMenu().hide();

        gameScene.optSubSceneFX().ifPresent(subSceneFX -> {
            subSceneFX.widthProperty().unbind();
            subSceneFX.heightProperty().unbind();
        });

        if (gameScene instanceof AbstractGameScene2D gameScene2D) {
            final DecorationPane frame = game.ui().viewManager().gamePlayView().gameSceneFrame();
            frame.canvas().widthProperty().unbind();
            frame.canvas().heightProperty().unbind();
            frame.unscaledWidthProperty().unbind();
            frame.unscaledHeightProperty().unbind();
            frame.backgroundProperty().unbind();
            gameScene2D.backgroundColorProperty().unbind();
            gameScene2D.scalingProperty().unbind();
        }
        Logger.info("Game scene {} REMOVED from play view!", gameScene.getClass().getSimpleName());
    }
}
