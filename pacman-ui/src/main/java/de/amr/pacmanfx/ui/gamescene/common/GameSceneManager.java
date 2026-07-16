/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Identifier;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.level.GameLevel;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d3.GameLevel3D;
import de.amr.pacmanfx.ui.gamescene.d3.PlayScene3D;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.uilib.model3D.pac.Pac3D;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.tinylog.Logger;

import java.util.Optional;

import static java.util.Objects.requireNonNull;

public class GameSceneManager {

    private GameAppContext appContext;

    private final ObjectProperty<GameScene> currentGameScene = new SimpleObjectProperty<>();

    public GameSceneManager() {
        currentGameScene.addListener((_, _, newGameScene) -> {
            if (newGameScene != null) {
                appContext.ui().views().gamePlayView().embedGameScene(newGameScene);
            }
        });
    }

    public void setGameActionContext(GameAppContext appContext) {
        this.appContext = requireNonNull(appContext);
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
        final GameVariantConfig variantConfig = appContext.variants().currentVariant().config();
        final GameModel model = appContext.currentGameContext().model();

        final GameScene currentGameScene = optCurrentGameScene().orElse(null);
        final GameScene nextGameScene = variantConfig.gameSceneConfig().selectGameScene(appContext, model).orElse(null);

        if (nextGameScene == null) {
            throw new IllegalStateException("Could not determine next game scene");
        }

        if (nextGameScene == currentGameScene) {
            if (!forceReload) {
                return;
            }
            Logger.info("No game scene change but reload requested");
        }

        appContext.ui().views().gamePlayView().replaceGameScene(currentGameScene, nextGameScene);

        //TODO rethink this
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

        final GameVariantConfig config = appContext.variants().currentVariant().config();
        return config.gameSceneConfig().gameSceneHasID(gameScene, sceneID);
    }

    /**
     * Checks whether the current game scene matches the given ID.
     *
     * @param sceneID scene identifier
     * @return {@code true} if the active scene has the given ID
     */
    public boolean currentGameSceneHasID(Identifier sceneID) {
        requireNonNull(sceneID);

        final GameScene currentGameScene = currentGameSceneProperty().get();
        return currentGameScene != null && hasGameSceneID(currentGameScene, sceneID);
    }

    // 2D-3D scene switch

    private void handle2D3DSwitch(
        GameVariantConfig variantConfig,
        GameLevel level,
        GameScene currentGameScene,
        GameScene nextGameScene)
    {
        final GameSceneSwitchType switchType = identifySwitchType(currentGameScene, nextGameScene);
        switch (switchType) {
            case FROM_2D_TO_3D -> switchPlaySceneTo3D(variantConfig, level, currentGameScene, nextGameScene);
            case FROM_3D_TO_2D -> switchPlaySceneTo2D(currentGameScene, nextGameScene);
            case NONE -> {}
            default -> throw new IllegalArgumentException("Illegal scene switch type: " + switchType);
        }
    }

    private void switchPlaySceneTo3D(
        GameVariantConfig variantConfig,
        GameLevel level,
        GameScene currentGameScene,
        GameScene nextGameScene)
    {
        if (!(nextGameScene instanceof PlayScene3D playScene3D)) {
            throw new IllegalArgumentException("Expected PlayScene3D, but scene has class %s"
                .formatted(nextGameScene.getClass().getSimpleName()));
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
            variantConfig.optSoundEffects().ifPresent(GameSoundEffects::playPacPowerSound);
        }
        playScene3D.fadeInAnimation().playFromStart();

        Logger.info("3D scene {} entered from 2D game scene {}", playScene3D.getClass().getSimpleName(), currentGameScene.getClass().getSimpleName());
    }

    private void switchPlaySceneTo2D(GameScene currentGameScene, GameScene nextGameScene) {
        if (!(nextGameScene instanceof AbstractGameScene2D playScene2D)) {
            throw new IllegalArgumentException("Expected GameScene2D, but scene has class %s"
                .formatted(nextGameScene.getClass().getSimpleName()));
        }
        playScene2D.onEnteredFrom3DScene();

        Logger.info("2D scene {} entered from 3D scene {}", playScene2D.getClass().getSimpleName(), currentGameScene.getClass().getSimpleName());
    }

    private GameSceneSwitchType identifySwitchType(GameScene currentGameScene, GameScene nextGameScene) {
        if (currentGameScene == null && nextGameScene == null) {
            throw new IllegalStateException("WTF is going on here, switch between NULL scenes?");
        }
        return switch (currentGameScene) {
            case AbstractGameScene2D _ when nextGameScene instanceof PlayScene3D         -> GameSceneSwitchType.FROM_2D_TO_3D;
            case PlayScene3D         _ when nextGameScene instanceof AbstractGameScene2D -> GameSceneSwitchType.FROM_3D_TO_2D;
            case null, default                                                           -> GameSceneSwitchType.NONE;
        };
    }
}
