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
import de.amr.pacmanfx.ui.views.GameViewManager;
import de.amr.pacmanfx.ui.views.playview.DecorationPane;
import de.amr.pacmanfx.ui.views.playview.GamePlayView;
import de.amr.pacmanfx.ui.window.GameMainScene;
import de.amr.pacmanfx.uilib.Ufx;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.SubScene;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameSceneManager {

    private Game game;

    private final ObjectProperty<GameScene> currentGameScene = new SimpleObjectProperty<>();

    public GameSceneManager() {
        currentGameScene.addListener((_, _, newGameScene) -> {
            if (newGameScene != null) {
                embedGameSceneIntoPlayView(newGameScene);
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
        final GameVariantConfig currentConfig = game.currentVariantConfig();
        final GameContext gameContext = game.currentGameContext();
        final GameModel gameModel = gameContext.model();

        final GameScene prevGameScene = optCurrentGameScene().orElse(null);
        final GameScene nextGameScene = currentConfig.gameSceneConfig().selectGameScene(game, gameModel).orElseThrow();

        if (nextGameScene == prevGameScene && !forceReload) {
            return;
        }

        if (prevGameScene != null) {
            prevGameScene.deactivate();
            removeFromPlayView(prevGameScene);
        }

        nextGameScene.onEmbedded(); // Must be called *before* embedding
        embedGameSceneIntoPlayView(nextGameScene);

        nextGameScene.activate();

        gameModel.optGameLevel().ifPresent(level -> handle2D3DSwitch(currentConfig, level, prevGameScene, nextGameScene));

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
        final GameVariantConfig currentConfig = game.currentVariantConfig();
        return currentConfig.gameSceneConfig().gameSceneHasID(gameScene, sceneID);
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

    private void handle2D3DSwitch(GameVariantConfig uiConfig, GameLevel level, GameScene prevGameScene, GameScene nextGameScene) {
        final GameSceneSwitchType sceneSwitchType = identifySceneSwitchType(prevGameScene, nextGameScene);
        switch (sceneSwitchType) {
            case FROM_2D_TO_3D -> switchPlaySceneTo3D(uiConfig, level, prevGameScene, nextGameScene);
            case FROM_3D_TO_2D -> switchPlaySceneTo2D(prevGameScene, nextGameScene);
            case NONE -> {}
            default -> throw new IllegalArgumentException("Illegal scene switch type: " + sceneSwitchType);
        }
    }

    private void switchPlaySceneTo3D(GameVariantConfig uiConfig, GameLevel level, GameScene currentScene, GameScene nextScene) {
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
            uiConfig.optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
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

        game.ui().views().gamePlayView().contextMenu().hide();

        gameScene.optSubSceneFX().ifPresent(subSceneFX -> {
            subSceneFX.widthProperty().unbind();
            subSceneFX.heightProperty().unbind();
        });
        if (gameScene instanceof AbstractGameScene2D gameScene2D) {
            final DecorationPane frame = game.ui().views().gamePlayView().gameSceneFrame();
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

    public void embedGameSceneIntoPlayView(GameScene gameScene) {
        final GameVariantConfig currentConfig = game.currentVariantConfig();
        final GameViewManager subViews = game.ui().views();

        subViews.gamePlayView().contextMenu().hide();

        if (gameScene.optSubSceneFX().isPresent()) {
            embedGameSceneWithSubSceneFX(subViews.gamePlayView(), gameScene, gameScene.optSubSceneFX().get());
        } else if (gameScene instanceof AbstractGameScene2D gameScene2D) {
            embedGameScene2D(currentConfig.gameSceneConfig(), gameScene2D);
        } else {
            Logger.error("Cannot embed play scene of class {}", gameScene.getClass().getName());
        }
    }

    // 3D scenes or 2D scenes with camera
    private void embedGameSceneWithSubSceneFX(GamePlayView playView, GameScene gameScene, SubScene subSceneFX) {
        final GameMainScene mainScene = game.ui().window().mainScene();

        // stretch sub scene to available space
        subSceneFX.widthProperty().bind(mainScene.widthProperty());
        subSceneFX.heightProperty().bind(mainScene.heightProperty());

        if (gameScene instanceof AbstractGameScene2D gameScene2D) {
            // use the canvas of the decorated pane for 2D scene even though the decoration is not used
            gameScene2D.setCanvas(playView.gameSceneFrame().canvas());
            playView.updateGameSceneRenderers(gameScene2D);
        }
        playView.setGameSceneContent(subSceneFX);
    }

    // 2D scenes without camera which are shown at full size
    private void embedGameScene2D(GameSceneConfig gameSceneConfig, AbstractGameScene2D gameScene2D) {
        final GameMainScene mainScene = game.ui().window().mainScene();
        final GamePlayView playView = game.ui().views().gamePlayView();
        final DecorationPane frame = playView.gameSceneFrame();

        gameScene2D.backgroundColorProperty().bind(game.ui().viewModel().common2D.canvasBackgroundColorProperty);

        final boolean decorated = gameSceneConfig.sceneDecorationRequested(gameScene2D);
        if (decorated) {
            frame.newCanvas(); //TODO check why creating a new canvas is needed
            frame.backgroundProperty().bind(gameScene2D.backgroundColorProperty().map(Ufx::paintBackground));

            // set unscaled decoration pane size to game scene (=world map) size
            frame.unscaledWidthProperty().bind(gameScene2D.unscaledWidthProperty());
            frame.unscaledHeightProperty().bind(gameScene2D.unscaledHeightProperty());

            // Limit scaling
            gameScene2D.scalingProperty().bind(frame.scalingProperty().map(
                scaling -> Math.min(scaling.doubleValue(), GamePlayView.MAX_GAME_SCENE_SCALING)));

            frame.stretchTo(mainScene.getWidth(), mainScene.getHeight());

            playView.setGameSceneContent(frame);
        }
        else {
            // Undecorated game scene taking complete height
            frame.canvas().heightProperty().bind(mainScene.heightProperty());

            frame.canvas().widthProperty().bind(mainScene.heightProperty()
                .map(h -> h.doubleValue() * gameScene2D.aspectRatio()));


            gameScene2D.scalingProperty().bind(mainScene.heightProperty().divide(gameScene2D.unscaledHeight()));

            playView.setGameSceneContent(frame.canvas());
        }

        gameScene2D.setCanvas(frame.canvas());
        playView.updateGameSceneRenderers(gameScene2D);
        frame.clearCanvas();
    }
}
