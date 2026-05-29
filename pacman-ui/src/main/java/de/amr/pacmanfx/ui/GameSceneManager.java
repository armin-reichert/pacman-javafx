/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui;

import de.amr.pacmanfx.core.CoinMechanism;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d3.GameLevel3D;
import de.amr.pacmanfx.ui.d3.PlayScene3D;
import de.amr.pacmanfx.ui.layout.playview.DecorationPane;
import de.amr.pacmanfx.ui.layout.playview.PlayView;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.UfxBackgrounds;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameSceneManager implements ChangeListener<GameScene> {

    private final ObjectProperty<GameScene> gameScene = new SimpleObjectProperty<>();

    private final GameUI ui;

    public GameSceneManager(GameUI ui) {
        this.ui = requireNonNull(ui);
        gameScene.addListener(this);
    }

    @Override
    public void changed(ObservableValue<? extends GameScene> py, GameScene oldGameScene, GameScene newGameScene) {
        if (newGameScene != null) {
            embedGameSceneIntoPlayView(newGameScene);
        }
    }

    public Optional<GameScene> optCurrentGameScene() {
        return Optional.ofNullable(gameScene.get());
    }

    public ObjectProperty<GameScene> gameSceneProperty() {
        return gameScene;
    }

    public void forceGameSceneUpdate() {
        updateGameSceneAndForceReload(true);
    }

    public void updateGameSceneAndForceReload(boolean forceReload) {
        final Game game = ui.gameContext().game();
        final GameScene prevGameScene = optCurrentGameScene().orElse(null);
        final GameScene nextGameScene = ui.currentConfig().gameSceneConfig().selectGameScene(ui, game).orElseThrow();

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

        game.optGameLevel().ifPresent(level -> handle2D3DSwitch(ui.currentConfig(), level, prevGameScene, nextGameScene));

        gameSceneProperty().set(nextGameScene);
    }

    public void quitCurrentGameScene() {
        final Game game = ui.gameContext().game();
        optCurrentGameScene().ifPresent(_ -> {
            final CoinMechanism coinMechanism = ui.gameContext().coinMechanism();
            //TODO Rethink this
            boolean shouldConsumeCoin = game.flow().state().name().equals("STARTING_GAME_OR_LEVEL")
                || game.isPlayingLevel();
            if (shouldConsumeCoin && !coinMechanism.isEmpty()) {
                coinMechanism.consumeCoin();
            }
            Logger.info("Quit game scene ({}), returning to start view", gameScene.getClass().getSimpleName());

        });
        ui.stopGame();
        ui.viewManager().selectStartView();
    }

    /**
     * Checks whether the given game scene matches the given ID.
     *
     * @param gameScene game scene
     * @param sceneID scene identifier
     * @return {@code true} if the active scene has the given ID
     */
    public boolean hasGameSceneID(GameScene gameScene, GameSceneConfig.SceneID sceneID) {
        requireNonNull(gameScene);
        requireNonNull(sceneID);
        return ui.currentConfig().gameSceneConfig().gameSceneHasID(gameScene, sceneID);
    }

    /**
     * Checks whether the current game scene matches the given ID.
     *
     * @param sceneID scene identifier
     * @return {@code true} if the active scene has the given ID
     */
    public boolean currentGameSceneHasID(GameSceneConfig.SceneID sceneID) {
        final GameScene current = gameSceneProperty().get();
        return current != null && hasGameSceneID(current, sceneID);
    }

    // 2D-3D scene switch

    private void handle2D3DSwitch(UIConfig uiConfig, GameLevel level, GameScene prevGameScene, GameScene nextGameScene) {
        final GameSceneSwitchType sceneSwitchType = identifySceneSwitchType(prevGameScene, nextGameScene);
        switch (sceneSwitchType) {
            case FROM_2D_TO_3D -> switchPlaySceneTo3D(uiConfig, level, prevGameScene, nextGameScene);
            case FROM_3D_TO_2D -> switchPlaySceneTo2D(prevGameScene, nextGameScene);
            case NONE -> {}
            default -> throw new IllegalArgumentException("Illegal scene switch type: " + sceneSwitchType);
        }
    }

    private void switchPlaySceneTo3D(UIConfig uiConfig, GameLevel level, GameScene currentScene, GameScene nextScene) {
        if (!(nextScene instanceof PlayScene3D playScene3D)) {
            throw new IllegalArgumentException("Expected PlayScene3D, but scene has class %s"
                .formatted(nextScene.getClass().getSimpleName()));
        }

        playScene3D.replaceGameLevel3D(level);
        playScene3D.updateHUD3D(level);
        playScene3D.replaceActionBindings(level);
        playScene3D.initFood3D(level.worldMap().foodLayer(), true);

        final GameLevel3D level3D = playScene3D.optGameLevel3D().orElseThrow();
        final Pac3D pac3D = level3D.entities().pac3D();
        playScene3D.initPac3D(pac3D, level);
        level3D.startLivesCounterTrackingPac();

        if (level.entities().pac().powerTimer().isRunning()) {
            uiConfig.optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
        }

        Logger.info("3D scene {} entered from 3D scene {}", playScene3D.getClass().getSimpleName(), currentScene.getClass().getSimpleName());

        playScene3D.fadeInAnimation().playFromStart();
    }

    private void switchPlaySceneTo2D(GameScene currentScene, GameScene nextScene) {
        if (!(nextScene instanceof GameScene2D playScene2D)) {
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
            case GameScene2D ignored when sceneAfter instanceof PlayScene3D -> GameSceneSwitchType.FROM_2D_TO_3D;
            case PlayScene3D ignored when sceneAfter instanceof GameScene2D -> GameSceneSwitchType.FROM_3D_TO_2D;
            case null, default -> GameSceneSwitchType.NONE; // may happen, it's ok
        };
    }

    // Scene embedding

    public void removeFromPlayView(GameScene gameScene) {
        requireNonNull(gameScene);

        ui.viewManager().playView().contextMenu().hide();

        gameScene.optSubSceneFX().ifPresent(subSceneFX -> {
            subSceneFX.widthProperty().unbind();
            subSceneFX.heightProperty().unbind();
        });
        if (gameScene instanceof GameScene2D gameScene2D) {
            final DecorationPane decorationPane = ui.viewManager().playView().decorationPane();
            decorationPane.canvas().widthProperty().unbind();
            decorationPane.canvas().heightProperty().unbind();
            decorationPane.unscaledWidthProperty().unbind();
            decorationPane.unscaledHeightProperty().unbind();
            decorationPane.backgroundProperty().unbind();
            gameScene2D.backgroundColorProperty().unbind();
            gameScene2D.scalingProperty().unbind();
        }

        Logger.info("Game scene {} REMOVED from play scene!", gameScene.getClass().getSimpleName());
    }

    public void embedGameSceneIntoPlayView(GameScene gameScene) {
        ui.viewManager().playView().contextMenu().hide();

        if (gameScene.optSubSceneFX().isPresent()) {
            embedGameSceneWithSubSceneFX(ui.scene(), ui.viewManager().playView(), gameScene, gameScene.optSubSceneFX().get());
        } else if (gameScene instanceof GameScene2D gameScene2D) {
            embedGameScene2D(ui.scene(), ui.viewManager().playView(), ui.currentConfig().gameSceneConfig(), gameScene2D);
        } else {
            Logger.error("Cannot embed play scene of class {}", gameScene.getClass().getName());
        }
    }

    // 3D scenes or 2D scenes with camera
    private void embedGameSceneWithSubSceneFX(Scene scene, PlayView playView, GameScene gameScene, SubScene subSceneFX) {
        // stretch sub scene to available space
        subSceneFX.widthProperty().bind(scene.widthProperty());
        subSceneFX.heightProperty().bind(scene.heightProperty());

        if (gameScene instanceof GameScene2D gameScene2D) {
            // use the canvas of the decorated pane for 2D scene even though the decoration is not used
            gameScene2D.setCanvas(playView.decorationPane().canvas());
            playView.updateGameSceneRenderers(gameScene2D);
        }
        playView.setGameSceneContent(subSceneFX);
    }

    // 2D scenes without camera which are shown at full size
    private void embedGameScene2D(Scene scene, PlayView playView, GameSceneConfig gameSceneConfig, GameScene2D gameScene2D) {
        final DecorationPane decorationPane = playView.decorationPane();

        gameScene2D.backgroundColorProperty().bind(GameUIConstants.PROPERTY_CANVAS_BACKGROUND_COLOR);

        final boolean decorated = gameSceneConfig.sceneDecorationRequested(gameScene2D);
        if (decorated) {
            decorationPane.newCanvas(); //TODO check why creating a new canvas is needed

            decorationPane.backgroundProperty().bind(gameScene2D.backgroundColorProperty().map(UfxBackgrounds::paintBackground));

            // set unscaled decoration pane size to game scene (=world map) size
            decorationPane.unscaledWidthProperty().bind(gameScene2D.unscaledWidthProperty());
            decorationPane.unscaledHeightProperty().bind(gameScene2D.unscaledHeightProperty());

            // Limit scaling
            gameScene2D.scalingProperty().bind(decorationPane.scalingProperty().map(
                scaling -> Math.min(scaling.doubleValue(), PlayView.MAX_GAME_SCENE_SCALING)));

            decorationPane.stretchTo(scene.getWidth(), scene.getHeight());

            playView.setGameSceneContent(decorationPane);
        }
        else {
            // Undecorated game scene taking complete height
            decorationPane.canvas().heightProperty().bind(scene.heightProperty());
            decorationPane.canvas().widthProperty().bind(scene.heightProperty().map(h -> h.doubleValue() * gameScene2D.getAspectRatio()));
            gameScene2D.scalingProperty().bind(scene.heightProperty().divide(gameScene2D.getUnscaledHeight()));

            playView.setGameSceneContent(decorationPane.canvas());
        }

        gameScene2D.setCanvas(decorationPane.canvas());
        playView.updateGameSceneRenderers(gameScene2D);
    }
}
